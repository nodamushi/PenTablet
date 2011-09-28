package nodamushi.pentablet;

import static java.awt.event.InputEvent.*;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Collection;

import javax.swing.SwingUtilities;

import jpen.PButtonEvent;
import jpen.PKind;
import jpen.PKindEvent;
import jpen.PLevel;
import jpen.PLevelEvent;
import jpen.PScroll;
import jpen.PScrollEvent;
import jpen.Pen;
import jpen.PenDevice;
import jpen.PenManager;
import jpen.PButton.Type;
import jpen.PButton.TypeGroup;
import jpen.event.PenListener;
import jpen.owner.multiAwt.AwtPenToolkit;
import nodamushi.pentablet.TabletMouseEvent.CursorDevice;
import nodamushi.pentablet.TabletMouseEvent.State;

final class JPenRecognizer implements PenListener,Listener{

	/**
	 * JPen2がきちんと動作しているか確認をします。<br>
	 * ただし、ここで0が返ってきた場合でもタブレットをまだ動作させていないだけの可能性があります。<br>
	 * タブレットを動かした後も0が返る場合は読みこまれてないかも
	 * @return 0:ネイティブライブラリが読み込まれていない可能性があります<br>
	 * 1:利用可能です。<br>
	 */
	public int canusetablet(){
		PenManager pm = AwtPenToolkit.getPenManager();
		Collection<PenDevice> c = pm.getDevices();
		if(c.size()==2)return 0;
		for(PenDevice p:c){
			if(!pm.isSystemMouseDevice(p)){
				if(p.getProvider().getConstructor().getNativeVersion() != -1)return 1;
			}
		}
		return 0;
	}

	private TabletRecognizer t;
	private Component c;
	private boolean set;
	private boolean isPress = true;
	private long waittime=0;
	private long time;
	private int keymodify=0;
	private MouseAdapter mouselistener = new MouseAdapter() {

		@Override
		public void mouseEntered(MouseEvent e) {
			TabletMouseEvent ae = TabletMouseEvent.wrapEvent(e);
			dump(ae);
		}

		@Override
		public void mouseExited(MouseEvent e) {
			TabletMouseEvent ae = TabletMouseEvent.wrapEvent(e);
			dump(ae);
		}
	};

	@Override
	public void setWaitTime(long t) {
		if(t<0)t=0;
		waittime = t;
	}



	private void dump(final TabletMouseEvent e){
		if(!SwingUtilities.isEventDispatchThread()){
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					switch (e.getState()) {
					case DRAGGED:
						t.mouseDragged(e);
						break;
					case MOVED:
						t.mouseMoved(e);
						break;
					case PRESSED:
						t.mousePressed(e);
						break;
					case RELEASED:
						t.mouseReleased(e);
						break;
					case ENTERED:
						if(!isPress)
							t.mouseEntered(e);
						break;
					case EXITED:
						if(!isPress)
							t.mouseExited(e);
						break;
					}
				}
			});
		}else{
			switch (e.getState()) {
			case ENTERED:
				if(!isPress)
					t.mouseEntered(e);
				break;
			case EXITED:
				if(!isPress)
					t.mouseExited(e);
				break;
			}
		}
	}

	void dump(final MouseWheelEvent e){
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				t.mouseWheelMoved(e);
			}
		});
	}


	JPenRecognizer(TabletRecognizer obj,Component target){
		t = obj;
		c=target;
		set();
	}

	public void remove(){
		if(set){
			c.removeMouseListener(mouselistener);
			AwtPenToolkit.removePenListener(c, this);
			set=false;
		}
	}
	public void set(){
		if(!set){
			c.addMouseListener(mouselistener);
			AwtPenToolkit.addPenListener(c, this);
			set=true;
		}
	}
	@Override
	public void dispose() {
		remove();
		t=null;c=null;
	}

	private static boolean isMousePressed(Pen pen){
		return pen.getButtonValue(Type.LEFT)|
				pen.getButtonValue(Type.CENTER)|
				pen.getButtonValue(Type.RIGHT);
	}
	final public void penButtonEvent(PButtonEvent e) {
		//version 2-110623から追加された。
		if(e.button.getType().getGroup()==TypeGroup.MODIFIER){
			int m=0;
			switch (e.button.getType()) {
			case ALT:
				m = ALT_DOWN_MASK|ALT_MASK;
				break;
			case CONTROL:
				m = CTRL_DOWN_MASK|CTRL_MASK;
				break;
			case SHIFT:
				m = SHIFT_DOWN_MASK|SHIFT_MASK;
				break;
			default:
				return;
			}
			if(e.pen.getButtonValue(e.button.getType())){
				keymodify|=m;
			}else{
				keymodify &= ~m;
			}
			return;
		}

		Pen pen = e.pen;
		PKind pkind = pen.getKind();
		int modify = keymodify;
		CursorDevice ctype;
		State state;
		isPress = isMousePressed(pen);
		switch(pkind.getType()){
		case STYLUS:
			ctype = CursorDevice.TABLET;
			modify |=TabletMouseEvent.HEAD_MASK;
			state = pen.getButtonValue(e.button.getType())?
					State.PRESSED:State.RELEASED;
			switch(e.button.getType()){
			case LEFT://無視
				return;
			case ON_PRESSURE:
				modify |=BUTTON1_DOWN_MASK|BUTTON1_MASK;
				break;
			case RIGHT:
				modify |=BUTTON3_DOWN_MASK|BUTTON3_MASK;
				break;
			case CENTER:
				modify |=BUTTON2_DOWN_MASK|BUTTON2_MASK;
				break;
			}

			break;
		case ERASER:
			ctype = CursorDevice.TABLET;
			modify |=TabletMouseEvent.TAIL_MASK;
			state = pen.getButtonValue(e.button.getType())?
					State.PRESSED:State.RELEASED;
			switch(e.button.getType()){
			case LEFT:
				return;
			case ON_PRESSURE:
				modify |=BUTTON1_DOWN_MASK|BUTTON1_MASK;
				break;
			case RIGHT:
				modify |=BUTTON3_DOWN_MASK|BUTTON3_MASK;
				break;
			case CENTER:
				modify |=BUTTON2_DOWN_MASK|BUTTON2_MASK;
				break;
			}
			break;
		case CUSTOM:
			return;
		case CURSOR:
			state =isPress? State.PRESSED:State.RELEASED;
			ctype = CursorDevice.MOUSE;
			switch(e.button.getType()){
			case LEFT:
				modify |=BUTTON1_MASK|BUTTON1_DOWN_MASK;break;
			case CENTER:
				modify |=BUTTON2_MASK|BUTTON2_DOWN_MASK;break;
			case RIGHT:
				modify |=BUTTON3_MASK|BUTTON3_DOWN_MASK;break;
			default:
				return;
			}
			break;
		default:
			return;
		}
		time=0;
		float x,y,p,r,tx,ty;
		x = pen.getLevelValue(PLevel.Type.X);//x取得
		y = pen.getLevelValue(PLevel.Type.Y);//y取得
		p = pen.getLevelValue(PLevel.Type.PRESSURE);//筆圧取得
		r = pen.getLevelValue(PLevel.Type.ROTATION);//rotation
		tx= pen.getLevelValue(PLevel.Type.TILT_X);//x傾き
		ty= pen.getLevelValue(PLevel.Type.TILT_Y);//y傾き
		TabletMouseEvent ev= TabletMouseEvent.createEvent(
				c, e.getTime(), modify, x, y, pen.getPressedButtonsCount(),
				ctype, state, p, r, tx, ty);
		dump(ev);

		//exit event
		if(!isPress){
			if(!c.contains((int)x, (int)y)){
				MouseEvent exit =
						new MouseEvent(
								c, MouseEvent.MOUSE_EXITED, System.currentTimeMillis(), keymodify,
								(int)x, (int)y, 0, 0, 0, false, 0);
				dump(TabletMouseEvent.wrapEvent(exit));
			}
		}
	}


	public void penKindEvent(PKindEvent e) {
		Pen pen = e.pen;
		float x,y,r,tx,ty;

		CursorDevice ctype;
		State state = State.CURSORTYPECHANGE;
		int modify =keymodify;
		switch(e.kind.getType()){
		case ERASER:
			modify |=TabletMouseEvent.TAIL_MASK;
			ctype = CursorDevice.TABLET;
			break;
		case STYLUS:
			modify |=TabletMouseEvent.HEAD_MASK;
			ctype = CursorDevice.TABLET;
			break;
		case CUSTOM:
			return;
		default:
			ctype = CursorDevice.MOUSE;
		}

		x = pen.getLevelValue(PLevel.Type.X);//x取得
		y = pen.getLevelValue(PLevel.Type.Y);//y取得
		r = pen.getLevelValue(PLevel.Type.ROTATION);//rotation
		tx= pen.getLevelValue(PLevel.Type.TILT_X);//x傾き
		ty= pen.getLevelValue(PLevel.Type.TILT_Y);//y傾き
		final TabletMouseEvent ev= TabletMouseEvent.createEvent(
				c, e.getTime(), modify, x, y, pen.getPressedButtonsCount(),
				ctype, state, 0, r, tx, ty);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				t.mouseOperatorChanged(ev);
			}
		});
	}

	public void penLevelEvent(PLevelEvent e) {
		if(waittime > time)return;
		time -=waittime;
		Pen pen = e.pen;


		PKind k = pen.getKind();
		CursorDevice ctype;
		State state;
		int modify = keymodify;
		switch(k.getType()){
		case STYLUS:
			modify |=TabletMouseEvent.HEAD_MASK;
			state = State.MOVED;
			ctype = CursorDevice.TABLET;
			if(pen.getButtonValue(Type.ON_PRESSURE)){
				modify |=BUTTON1_DOWN_MASK|BUTTON1_MASK;
				state = State.DRAGGED;
			}
			if(pen.getButtonValue(Type.CENTER)){
				modify |=BUTTON2_DOWN_MASK|BUTTON2_MASK;
				state = State.DRAGGED;
			}
			if(pen.getButtonValue(Type.RIGHT)){
				modify |=BUTTON3_DOWN_MASK|BUTTON3_MASK;
				state = State.DRAGGED;
			}
			break;
		case ERASER:
			modify |=TabletMouseEvent.TAIL_MASK;
			state = State.MOVED;
			ctype = CursorDevice.TABLET;
			if(pen.getButtonValue(Type.ON_PRESSURE)){
				modify |=BUTTON1_DOWN_MASK|BUTTON1_MASK;
				state = State.DRAGGED;
			}
			if(pen.getButtonValue(Type.CENTER)){
				modify |=BUTTON2_DOWN_MASK|BUTTON2_MASK;
				state = State.DRAGGED;
			}
			if(pen.getButtonValue(Type.RIGHT)){
				modify |=BUTTON3_DOWN_MASK|BUTTON3_MASK;
				state = State.DRAGGED;
			}
			break;
		case CUSTOM:
			return;
		case CURSOR:
			ctype = CursorDevice.MOUSE;
			state = isMousePressed(pen)?State.DRAGGED:State.MOVED;
			if(pen.getButtonValue(Type.LEFT)){
				modify |=BUTTON1_DOWN_MASK|BUTTON1_MASK;
			}
			if(pen.getButtonValue(Type.CENTER)){
				modify |=BUTTON2_DOWN_MASK|BUTTON2_MASK;
			}
			if(pen.getButtonValue(Type.RIGHT)){
				modify |=BUTTON3_DOWN_MASK|BUTTON3_MASK;
			}
			break;
		default:
			return;
		}


		float x,y,p,r,tx,ty;

		x = pen.getLevelValue(PLevel.Type.X);//x取得
		y = pen.getLevelValue(PLevel.Type.Y);//y取得
		p = pen.getLevelValue(PLevel.Type.PRESSURE);//筆圧取得
		r = pen.getLevelValue(PLevel.Type.ROTATION);//rotation
		tx= pen.getLevelValue(PLevel.Type.TILT_X);//x傾き
		ty= pen.getLevelValue(PLevel.Type.TILT_Y);//y傾き
		TabletMouseEvent ev= TabletMouseEvent.createEvent(
				c, e.getTime(), modify, x, y, pen.getPressedButtonsCount(),
				ctype, state, p, r, tx, ty);
		dump(ev);
	}


	public void penScrollEvent(PScrollEvent e) {
		int val = e.scroll.value;
		if(val==0)return;
		int rotation =e.scroll.getType()==PScroll.Type.DOWN?
				1:-1;
		Pen pen = e.pen;
		int x = (int) pen.getLevelValue(PLevel.Type.X);//x取得
		int y = (int) pen.getLevelValue(PLevel.Type.Y);//y取得
		dump(new MouseWheelEvent(c, MouseEvent.MOUSE_WHEEL, e.getTime(), keymodify,
				x, y, pen.getPressedButtonsCount(), false,
				0, val, rotation));

	}

	public void penTock(long n) {
		if(waittime!=0)
			time+=n;
	}

}