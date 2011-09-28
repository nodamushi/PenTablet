package nodamushi.pentablet;

import java.awt.Component;
import java.awt.event.MouseWheelEvent;

/**
 * PenTabletRecognizerのアダプタークラスです。<br>
 * ユーザーは必要なメソッドだけオーバーライドできます。
 * @author nodamushi
 *
 */
public abstract class TabletAdapter extends TabletRecognizer{
	public TabletAdapter(Component c) {super(c);}
	@Override public void mouseDragged(TabletMouseEvent e) {}
	@Override public void mouseMoved(TabletMouseEvent e) {}
	@Override public void mousePressed(TabletMouseEvent e) {}
	@Override public void mouseReleased(TabletMouseEvent e) {}
	@Override public void mouseWheelMoved(MouseWheelEvent e) {}
	@Override public void mouseOperatorChanged(TabletMouseEvent e) {}
	@Override public void mouseEntered(TabletMouseEvent e) {}
	@Override public void mouseExited(TabletMouseEvent e) {}
}
