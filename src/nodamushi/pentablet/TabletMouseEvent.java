package nodamushi.pentablet;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

public final class TabletMouseEvent extends MouseEvent{



	private static final long serialVersionUID = 5560345349890009137L;
	private int x,y;
	private double dx,dy;
	private CursorDevice cursorDevice;
	private State state;
	private double
		pressure=1,//筆圧
		titx=0,tity=0,//ペンの傾き
		rotation=0;//回転角度

	//Mouse用
	private TabletMouseEvent(Component source,int id,long when,int modifiers,
            int x,int y,int clickCount,boolean popupTrigger,int button,
            CursorDevice ctype,State state)
	{
		super(source,id,when,modifiers,x,y,clickCount,popupTrigger,button);
		dx = x+0.5f;dy=y+0.5f;
		this.x=x;this.y=y;
		cursorDevice=ctype;
		this.state = state;
	}


	private TabletMouseEvent(Component source,int id,long when,int modifiers,
            double x,double y,int clickCount,boolean popupTrigger,
            CursorDevice ctype,State state,
            double pres,double rot,double tx,double ty)
	{
		super(source,id,when,modifiers,(int)x,(int)y,clickCount,popupTrigger);
		setPoint(x,y);
		cursorDevice=ctype;
		this.state = state;
		if(ctype == CursorDevice.TABLET)
		{
			pressure = pres;
			titx = tx;
			tity = ty;
			rotation = rot;
		}
	}



	/**
	 * このイベントの発生元がマウスによる物かどうかを返します
	 * @see CursorDevice
	 * @see #getCursorDevice()
	 */
	public boolean isMouseDevice(){return cursorDevice == CursorDevice.MOUSE;}
	/**
	 * このイベントの発生元がタブレットによる物かどうかを返します
	 * @see CursorDevice
	 * @see #getCursorDevice()
	 */
	public boolean isTabletDevice(){return cursorDevice == CursorDevice.TABLET;}

	/**
	 * このインスタンスの保持する点を変更します
	 * @param x 変更後のx座標
	 * @param y 変更後のy座標
	 * @see #getX()
	 * @see #getY()
	 * @see #getXY()
	 * @see #getXYDouble()
	 */
	public void setPoint(double x,double y){
		dx = x;dy=y;
		this.x=(int)x;this.y=(int)y;
	}
	/**
	 * このインスタンスの保持する点を変更します
	 * @param p 変更後の座標
	 * @see #getX()
	 * @see #getY()
	 * @see #getXY()
	 * @see #getXYDouble()
	 */
	public void setPoint(Point.Double p){if(p != null)setPoint(p.x,p.y);}
	/**
	 * このインスタンスの保持する点を変更します
	 * @param x 変更後のx座標
	 * @param y 変更後のy座標
	 * @see #getX()
	 * @see #getY()
	 * @see #getXY()
	 * @see #getXYDouble()
	 */
	public void setPoint(int x,int y)
	{
		this.x = x;
		this.y = y;
		dx = x;
		dy = y;
	}
	/**
	 * このインスタンスの保持する点を変更します
	 * @param p 変更後の座標
	 * @see #getX()
	 * @see #getY()
	 * @see #getXY()
	 * @see #getXYDouble()
	 */
	public void setPoint(Point p){ if(p != null)setPoint(p.x,p.y); }
	/**筆圧を設定します*/
	public void setPressure(double p){pressure = p;}
	/**x方向の傾きを設定します*/
	public void setAlititudeX(double a){titx  = a;}
	/**y方向の傾きを設定します*/
	public void setAlititudeY(double a){tity  = a;}
	/**回転角度を設定します*/
	public void setRotation(double a){rotation = a;}

	@Override
	public Point getPoint() {return new Point(x,y);}

	/**
	 * <p>マウス座標をPoint2D.Doubleで返します。</p>
	 */
	public Point2D.Double getPoint2D() {
		return new Point2D.Double(dx, dy);
	}

	public double getDoubleX(){
		return dx;
	}
	public double getDoubleY(){
		return dy;
	}


	/**
	 * ペンの筆圧を返します。
	 */
	public double getPressure(){return pressure;}
	/**
	 * ペンの傾きを取得します。
	 */
	public double getAlititudeX(){return titx;}
	/**
	 * ペンの傾きを取得します。
	 */
	public double getAlititudeY(){return tity;}

	/**
	 * ペンが北（上方向）から時計回りに何度の方向を向いているか
	 */
	public double getRotation(){return rotation;}

	/**
	 * このイベントを発生させたデバイスを返します。
	 * @see CursorDevice
	 */
	public CursorDevice getCursorDevice(){return cursorDevice;}
	/**
	 * このマウスイベントの状態を返します。
	 * @see State
	 * @see #getID()
	 */
	public State getState(){return state;}




	@Override
	public String toString() {
		return String.format("(x:%f,y:%f),pressure %f,tit(x %f,y %f),rotation %f,CursorType %s,State %s",dx,dy,pressure,titx,tity,rotation,cursorDevice.toString(),state.toString());
	}


	/**
	 * ペン（先端）でクリックされたかどうか。
	 * @return ペンでクリックされた場合true。マウスのBUTTON1がクリックされていてもtrueが返ります。
	 * ペンデバイスでも、RIGHTやCENTERの時はtrueが返りません。
	 */
	public boolean isPenPressed(){
		int m = getModifiers()|getModifiersEx();
		return b(m,HEAD_DOWN_MASK)||
				((b(m,BUTTON1_DOWN_MASK)||b(m,BUTTON1_MASK)) &&!b(m,TAIL_MASK));
	}

	public boolean isPenDevice(){
		int m = getModifiersEx();
		return b(m,HEAD_MASK);
	}

	/**
	 * ペンの後ろでクリックされたかどうか。
	 * @return
	 */
	public boolean isTailPressed(){
		int m = getModifiersEx();
		return b(m,TAIL_DOWN_MASK);
	}
	public boolean isTailDevice(){
		int m = getModifiersEx();
		return b(m,TAIL_MASK);
	}

	private static boolean b(int m,int mask){
		return (m&mask) == mask;
	}





	//****************************************************************//
	//************************static**********************************//
	//****************************************************************//

	/**
	 * CURSORTYPECHANGEのイベントid番号
	 */
	static public final int MOUSE_CURSORTYPECHANGE=510;
	static public final int HEAD_MASK=32768,
			HEAD_DOWN_MASK = HEAD_MASK|BUTTON1_DOWN_MASK,
			TAIL_MASK=65536,
			TAIL_DOWN_MASK = TAIL_MASK|BUTTON1_DOWN_MASK;

	/**
	 * 前後関係を指定し、マウスイベントをラッピングします
	 * @param e 元となるマウスイベント
	 * @param befo リストの前方
	 * @param nex リストの後方
	 * @return ラップしたAPainterMouseEvent
	 */
	static public TabletMouseEvent wrapEvent(MouseEvent e)
	{
		State state;
		int id = e.getID();
		state = State.getState(id);
		return new TabletMouseEvent((Component)e.getSource(), id, e.getWhen(),
				e.getModifiers(), e.getX(), e.getY(),
				e.getClickCount(), e.isPopupTrigger(), e.getButton(),  CursorDevice.MOUSE, state);
	}



	/**
	 *
	 * @param source
	 * @param when
	 * @param modifiers shift,alt,meta,ctrのダウンマスクを設定するだけで十分です。（BUTTON1_DOWN_MASK等は自動で設定します）
	 * @param x
	 * @param y
	 * @param clickCount
	 * @param btype
	 * @param ctype
	 * @param state
	 * @param pres
	 * @param rot
	 * @param tx
	 * @param ty
	 * @return
	 */
	static TabletMouseEvent createEvent(Component source,long when,int modifiers,double x,double y,
			int clickCount,CursorDevice ctype,State state,
			double pres,double rot,double tx,double ty)
	{
		boolean popupTrigger=b(modifiers,BUTTON3_DOWN_MASK)||b(modifiers,BUTTON3_MASK);
		if(pres <0)pres = 0;
		else if(pres >1)pres = 1;
		int id=state.getID();
		return new TabletMouseEvent(source, id, when, modifiers, x, y,
				clickCount, popupTrigger, ctype, state, pres, rot, tx, ty);
	}

	static public TabletMouseEvent createEvent(Component source,long when,int modifiers,double x,double y,
			int clickCount,CursorDevice ctype,State state)
	{
		return createEvent(source, when, modifiers, x, y, clickCount, ctype, state, 1, 0, 0, 0);
	}


	//enums/////////////////////////////////////////////////////////////////////
	/**
	 * カーソルを動かしているデバイス
	 */
	static public enum CursorDevice{
		TABLET,MOUSE
	}
	/**
	 * ボタンの状態
	 */
	static public enum State{
		PRESSED,DRAGGED,RELEASED,ENTERED,EXITED,MOVED,
		/**カーソルを動かしているデバイスが変わった*/CURSORTYPECHANGE,
		/**判別不能の時（nullの代用）*/NULL,
		/**非推奨。マウスイベントとの整合性を保つ為にあります。今後もタブレットでこのイベントは発生させません*/CLICKED;
		/**
		 * 対応するMouseイベントのidを返します。
		 * @return
		 */
		public int getID(){ return getID(this); }
		/**
		 * 渡されたstateに対応するMouseイベントのidを返します
		 */
		public static int getID(State state){
			int id=0;
			switch (state) {
			case PRESSED:
				id = MOUSE_PRESSED;
				break;
			case DRAGGED:
				id = MOUSE_DRAGGED;
				break;
			case RELEASED:
				id = MOUSE_RELEASED;
				break;
			case MOVED:
				id = MOUSE_MOVED;
				break;
			case CURSORTYPECHANGE:
				id = MOUSE_CURSORTYPECHANGE;
				break;
			case CLICKED:
				id = MOUSE_CLICKED;
				break;
			case ENTERED:
				id = MOUSE_ENTERED;
				break;
			case EXITED:
				id = MOUSE_EXITED;
				break;
			}
			return id;
		}

		/**
		 * 渡されたMouseイベントのidに対応するStateを返します
		 */
		public static State getState(int id){
			State state;
			switch(id){
			case MOUSE_CURSORTYPECHANGE:
				state = CURSORTYPECHANGE;
				break;
			case MOUSE_CLICKED:
				state =State.CLICKED;
				break;
			case MOUSE_DRAGGED:
				state = State.DRAGGED;
				break;
			case MOUSE_ENTERED:
				state = State.ENTERED;
				break;
			case MOUSE_EXITED:
				state = State.EXITED;
				break;
			case MOUSE_MOVED:
				state = State.MOVED;
				break;
			case MOUSE_PRESSED:
				state = State.PRESSED;
				break;
			case MOUSE_RELEASED:
				state = State.RELEASED;
				break;
			default:
				state = State.NULL;
			}
			return state;
		}
	}
}
