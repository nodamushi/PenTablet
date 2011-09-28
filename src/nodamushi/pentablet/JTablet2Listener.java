package nodamushi.pentablet;

import java.awt.Component;
import java.awt.event.MouseWheelEvent;

import javax.swing.SwingUtilities;

import nodamushi.pentablet.TabletMouseEvent.CursorDevice;
import nodamushi.pentablet.TabletMouseEvent.State;
import cello.jtablet.TabletManager;
import cello.jtablet.event.TabletEvent;
import cello.jtablet.event.TabletListener;
import cello.jtablet.installer.JTabletExtension;

public class JTablet2Listener implements TabletListener,Listener{

	private Component c;
	private TabletRecognizer t;
	boolean set=false;
	static boolean canUseJTablet2(){
		try{
			Class.forName("cello.jtablet.installer.JTabletExtension");
			String version = JTabletExtension.getInstalledVersion();
			String[] v = version.split("-")[0].split("\\.");
			if(Integer.parseInt(v[0])<1)return false;
			return true;
		}catch(Exception e){
			return false;
		}
	}

	public JTablet2Listener(TabletRecognizer tr,Component target) {
		c = target;
		t = tr;
		set();
	}

	@Override
	public void set() {
		if(!set){
			set=true;
			TabletManager.getDefaultManager().addTabletListener(c, this);
		}

	}

	@Override
	public void remove() {
		if(set){
			set=false;
			TabletManager.getDefaultManager().removeTabletListener(c, this);
		}

	}

	@Override
	public void dispose() {
		remove();
		c=null;
	}

	@Override
	public int canusetablet() {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

	@Override
	public void setWaitTime(long t) {
		// TODO 自動生成されたメソッド・スタブ

	}

	private TabletMouseEvent convert(TabletEvent event,State state){
		float x,y,tx,ty,rot,pre;
		x = event.getFloatX();
		y = event.getFloatY();
		tx = event.getTiltX();
		ty = event.getTiltY();
		pre = event.getPressure();
		rot =event.getType()==TabletEvent.Type.ROTATED?0f: event.getRotation();
		CursorDevice ctype;
		int modify = event.getModifiers()|event.getModifiersEx();
		switch (event.getDevice().getType()) {
		case STYLUS:
			ctype = CursorDevice.TABLET;
			modify|=TabletMouseEvent.HEAD_MASK;
			break;
		case ERASER:
			modify|=TabletMouseEvent.TAIL_MASK;
			ctype = CursorDevice.TABLET;
			break;
		default:
			ctype =CursorDevice.MOUSE;
			break;
		}
		return TabletMouseEvent.createEvent(
				event.getComponent(), event.getWhen(),
				modify, x, y,
				event.getClickCount(), ctype,
				state, pre, rot, tx, ty);
	}
	private boolean pressed=false;

	@Override
	public void cursorPressed(TabletEvent event) {
		pressed = true;
		TabletMouseEvent e = convert(event, State.PRESSED);
		dump(e);
	}

	@Override
	public void cursorReleased(TabletEvent event) {
		pressed = false;
		TabletMouseEvent e = convert(event, State.RELEASED);
		dump(e);
	}

	@Override
	public void cursorEntered(TabletEvent event) {
		TabletMouseEvent e = convert(event, State.ENTERED);
		dump(e);
	}

	@Override
	public void cursorExited(TabletEvent event) {
		TabletMouseEvent e = convert(event, State.EXITED);
		dump(e);
	}

	@Override
	public void cursorMoved(TabletEvent event) {
		TabletMouseEvent e = convert(event, State.MOVED);
		dump(e);
	}

	@Override
	public void cursorDragged(TabletEvent event) {
		TabletMouseEvent e = convert(event, State.DRAGGED);
		dump(e);
	}

	@Override
	public void cursorScrolled(TabletEvent event) {
		int a,r;
		r= (int) event.getScrollY();
		if(r<0){
			a = -1;
			r= -r;
		}else{
			a = 1;
		}
		MouseWheelEvent e = new MouseWheelEvent(event.getComponent(), event.getID(),
				event.getWhen(), event.getModifiers(), event.getX(), event.getY(),
				event.getClickCount(), event.isPopupTrigger(),
				MouseWheelEvent.WHEEL_UNIT_SCROLL, a, r);
		dump(e);
	}

	@Override
	public void cursorGestured(TabletEvent event) {
	}

	@Override
	public void levelChanged(TabletEvent event) {
		TabletMouseEvent e = convert(event, pressed?State.DRAGGED:State.MOVED);
		dump(e);
	}


	private void dump(final TabletMouseEvent e){
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
			t.mouseEntered(e);
			break;
		case EXITED:
			t.mouseExited(e);
			break;
		}
	}

	void dump(final MouseWheelEvent e){
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				t.mouseWheelMoved(e);
			}
		});
	}

}
