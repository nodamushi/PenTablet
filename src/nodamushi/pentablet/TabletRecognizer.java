package nodamushi.pentablet;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import jpen.PenManager;

/**
 * コンポーネントにタブレットの情報を受け取れるようにする為のリスナークラスです。<br>
 * リスナーはこのクラスを拡張してください。<br><br>
 * TabletLintenerはjpen2をラップするクラスになります。jpen2がインストールされていない環境では通常のマウスリスナーとして振る舞います
 * 。<br>
 * ユーザーはjpen2の存在や使い方と言った事を意識する必要はありません。<br>
 * なお、TabletListenerと通常のマウスイベントリスナーとの共存は動作を保証しません。<br>
 * @see TabletMouseEvent
 * @author nodamushi
 * @version 1.2.2
 */
public abstract class TabletRecognizer implements MouseWheelListener{

	/**
	 * PenTabletRecognizerのバージョン情報　"x_y_z"
	 */
	public static final String Version = "1_2_2";


	/**
	 *
	 * @param target イベントを受け取るコンポーネント
	 * @throws IllegalArgumentException targetがnullの時
	 */
	public TabletRecognizer(Component target) throws IllegalArgumentException{
		if(target == null)throw new IllegalArgumentException("target component is null!");
		this.target = target;
		ujpen = enablejpen?true:searchJPen();
		if(ujpen){
			listener=new JPenRecognizer(this, target);
		}else{
			ujtablet2 = JTablet2Listener.canUseJTablet2();
			if(ujtablet2){
				listener = new JTablet2Listener(this, target);
			}else
				listener=new MouseRecognizer(this);
		}
	}

	/**
	 * このインスタンスが通常のマウスリスナーとして動作している場合、JPenのリスナーとして動くようにします。<br>
	 * searchJPenによりjpen2の利用可能状態が利用不可から可に変化したときに呼び出してください。
	 * @return このインスタンスがJPenが利用可能になったかどうか。
	 */
	public final synchronized boolean reInstall(){
		if(enablejpen && !ujpen){
			removeListener();
			listener = new JPenRecognizer(this, target);
			ujpen = true;
			setListener();
		}
		return ujpen;
	}


	/**
	 * このインスタンスがJPenを利用しているかどうかを返します
	 * @return このインスタンスがJPenを利用しているかどうか
	 */
	public final boolean isUseJPen(){
		return ujpen;
	}

	public final void setWaitTime(long time){
		listener.setWaitTime(time);
	}

	/**
	 * ターゲットコンポーネントからイベントを受け取らないようにします
	 */
	public final void removeListener(){
		listener.remove();
	}

	/**
	 * 破棄し、使用不可能にします。
	 */
	public final void dispose(){
		listener.dispose();
		_dispose();
	}

	/**
	 * disposeメソッドが呼ばれたときに、
	 * 何らかの処理をしたい場合はこのメソッドをオーバーライドして下さい。
	 */
	protected void _dispose(){}

	/**
	 * コンストラクターで指定したターゲットコンポーネントからイベントを受け取れるようにします。<br>
	 * removeListenerで受け取らなくした後、再び受け取りたい場合は呼び出してください
	 */
	public final void setListener(){
		listener.set();
	}


	/**
	 * JPen2がきちんと動作しているか確認をします。<br>
	 * ただし、ここで0が返ってきた場合でもタブレットをまだ動作させていないだけの可能性があります。<br>
	 * タブレットを動かした後も0が返る場合は読みこまれてないかも
	 * @return 0:ネイティブライブラリが読み込まれていない可能性があります<br>
	 * 1:利用可能です。<br>
	 * -1:jpen2が使えません。
	 */
	public final int isjpenloadnative(){
		return listener.canusetablet();
	}

	////////////////////////////////////////////////////////メンバ
	/**
	 * リスナーの対象となるコンポーネント
	 */
	protected final Component target;
	private Listener listener;
	private boolean ujpen;//jpenを使っているかどうか
	private boolean ujtablet2;//jtablet2を使っているかどうか

	////////////////////////////////////////////////////////static
	static private final int[] JPenVersion = {2};
	static private final int[] SubVersion={110623};

	static private boolean enablejpen;

	/**
	 * JPenが使えるかどうか調べます。<br>
	 * JPenが使えない状態から使える状態に変化しても、これまでに作成したPenTabletRecognizerのインスタンスはペンタブレットのイベントを受け取りません。<br>
	 * これまでに生成したインスタンスも受け取れるようにするにはreInstallメソッドを呼び出してください。
	 * @return
	 */
	static public boolean searchJPen(){
		boolean b = false;
		TRY:try {
			Class.forName("jpen.PenManager");
			String[] s = PenManager.getJPenFullVersion().split("-");
			int[] v = {Integer.parseInt(s[0]),Integer.parseInt(s[1])};
			int i=0;
			for(int jv:JPenVersion){
				if(jv==v[0]){
					b= true;
					break;
				}
				i++;
			}
			if(!b){
				break TRY;
			}
			b= SubVersion[i]<=v[1];
		} catch (ClassNotFoundException e) {
			b =false;
		}
		enablejpen = b;
		return b;
	}


	public static String getJPenFullVersion(){
		if(enablejpen)return PenManager.getJPenFullVersion();
		else return "";
	}

	/**
	 * このPenTabletRecognizerが対応しているJPenのバージョンを返します
	 * @return
	 */
	public static int[] getCompatibleJPenVersion(){
		return JPenVersion.clone();
	}


	//////////////////////////継承用の関数///////////////////////////////////////

	/**
	 * ドラッグが起こると呼び出される関数です。
	 *
	 * @param e
	 * マウスイベント
	 */
	public abstract void mouseDragged(TabletMouseEvent e);

	/**
	 * ボタンが押されていない、もしくは筆圧が無い状態でマウスの移動が起こると呼び出される関数です。
	 *
	 * @param e
	 */
	public abstract void mouseMoved(TabletMouseEvent e);

	/**
	 * ボタンが押された、筆圧を感知した時に呼び出される関数です。
	 *
	 * @param e
	 */
	public abstract void mousePressed(TabletMouseEvent e);

	/**
	 * ボタンが離された、筆圧がなくなった時に呼び出される関数です。
	 *
	 * @param e
	 */
	public abstract void mouseReleased(TabletMouseEvent e);


	/**
	 * ペン先から消しゴムやマウスとかにカーソルを操作するものが変わったときに呼び出される。
	 *
	 * @param e
	 */
	public abstract void mouseOperatorChanged(TabletMouseEvent e);

	/**
	 * マウスが領域に入ったときに起こるイベントです。
	 *
	 * @param e
	 */
	public abstract void mouseEntered(TabletMouseEvent e);

	/**
	 * マウスが領域から出たときに起こるイベントです。
	 *
	 * @param e
	 */
	public abstract void mouseExited(TabletMouseEvent e);

}
interface Listener{
	void set();
	void remove();
	void dispose();
	int canusetablet();
	void setWaitTime(long t);
}

//------------------------------------------------
//ただのリスナー
//------------------------------------------------
final class MouseRecognizer implements MouseListener,MouseMotionListener,MouseWheelListener,Listener{
	MouseRecognizer(TabletRecognizer t){this.t=t;set();}

	public void mousePressed(MouseEvent e) {
		t.mousePressed(TabletMouseEvent.wrapEvent(e));
	}


	public void mouseReleased(MouseEvent e) {
		t.mouseReleased(TabletMouseEvent.wrapEvent(e));
	}

	public void mouseMoved(MouseEvent e) {
		t.mouseMoved(TabletMouseEvent.wrapEvent(e));
	}

	public void mouseDragged(MouseEvent e) {
		t.mouseDragged(TabletMouseEvent.wrapEvent(e));
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		t.mouseWheelMoved(e);
	}
	public void mouseEntered(MouseEvent e) {
		t.mouseEntered(TabletMouseEvent.wrapEvent(e));
	}
	public void mouseExited(MouseEvent e) {
		t.mouseExited(TabletMouseEvent.wrapEvent(e));
	}
	public void mouseClicked(MouseEvent e) {}
	@Override
	public void remove() {
		t.target.removeMouseListener(this);
		t.target.removeMouseMotionListener(this);
		t.target.removeMouseWheelListener(this);
	}

	@Override
	public void set() {
		t.target.addMouseListener(this);
		t.target.addMouseMotionListener(this);
		t.target.addMouseWheelListener(this);
	}


	@Override
	public int canusetablet() {
		return -1;
	}
	@Override
	public void dispose() {
		remove();
		t=null;
	}
	@Override
	public void setWaitTime(long t) {
	}

	TabletRecognizer t;
}

