package atissue;

/**
 * 
 * @author Wai Fai Chin
 * created on 2015-02-02
 */

public class SerialPortSetting {
	public final static int PARITY_NONE = 0;
	public final static int PARITY_EVEN = 2;
	public final static int PARITY_ODD	= 1;
	public final static int DISCONNECTED = 0;
	public final static int CONNECTED = 1;
	public final static int FLOWCONTROL_NONE = 0;
	public final static int FLOWCONTROL_RTSCTS = 3;
	public final static int FLOWCONTROL_XONXOFF = 12;
	public final static int DATA_BIT7	= 7;
	public final static int DATA_BIT8	= 8;
	public final static int STOP_BIT1	= 1;
	public final static int STOP_BIT2	= 2;

	public int baudRate;
	public int dataBits;
	public int stopBits;
	public int parity;
	public int flowControl;
	public String comportName;
	public int status;
}
