package atissue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.bluetooth.UUID;

import org.eclipse.kura.comm.CommConnection;
import org.eclipse.kura.comm.CommURI;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.io.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForMyIssue {
	private static final Logger s_logger = LoggerFactory.getLogger(ForMyIssue.class);
	public  ConnectionFactory m_connectionFactory;
	private SerialPortSetting m_setting;
	private CommConnection m_commConnection;
	private InputStream commIs;
	private OutputStream commOs;
	
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.m_connectionFactory = connectionFactory;
	}

	public void unsetConnectionFactory(ConnectionFactory connectionFactory) {
		this.m_connectionFactory = null;
	}

	protected void activate(ComponentContext componentContext) {
		s_logger.info("Activating BtServer...");
		
		m_setting = new SerialPortSetting();
		m_setting.baudRate = 38400;
		m_setting.dataBits = 8;
		m_setting.stopBits = 1;
		m_setting.parity = SerialPortSetting.PARITY_NONE;
		m_setting.flowControl = SerialPortSetting.FLOWCONTROL_NONE; 
		m_setting.comportName = "/dev/ttyUSB0";
		m_setting.status = SerialPortSetting.DISCONNECTED;
		
		openPort();

		s_logger.info("Activating BtServer... Done.");
	}

	protected void deactivate(ComponentContext componentContext) {
		s_logger.info("Deactivating BtServer...");
		closePort();
		s_logger.info("Deactivating BtServer... Done.");
	}
	
	private void openPort() {

		UUID uuid = new UUID("1101", true);
		
		String uri = new CommURI.Builder(m_setting.comportName)
			.withBaudRate(m_setting.baudRate)
			.withDataBits(m_setting.dataBits)
			.withStopBits(m_setting.stopBits)
			.withParity(m_setting.parity)
			.withTimeout(1000)
			.build().toString();

		try {
			m_commConnection = (CommConnection) m_connectionFactory.createConnection(uri, 1, false);
			commIs = m_commConnection.openInputStream();
			commOs = m_commConnection.openOutputStream();
			s_logger.info(m_setting.comportName + " open");
			m_setting.status = SerialPortSetting.CONNECTED;

		} catch (IOException e) {
			s_logger.error("Failed to open port " + m_setting.comportName + e.getMessage());

		}
		
//		String connectionString = "btspp://localhost:" + uuid + ";name=Bluetooth Server";
//		StreamConnectionNotifier streamConnNotifier = (StreamConnectionNotifier) Connector.open(connectionString);
//		s_logger.info("\nServer Started. Waiting for clients to connect...");
	
	}
	
	private void closePort() {

		if (commIs != null) {
			try {
				s_logger.info("Closing port input stream...");
				commIs.close();
				s_logger.info("Closed port input stream");
			} catch (IOException e) {
				s_logger.error("Cannot close port input stream", e);
			}
			commIs = null;
		}

		if (commOs != null) {
			try {
				s_logger.info("Closing port output stream...");
				commOs.close();
				s_logger.info("Closed port output stream");
			} catch (IOException e) {
				s_logger.error("Cannot close port output stream", e);
			}
			commOs = null;
		}

		if (m_commConnection != null) {
			try {
				s_logger.info("Closing port...");
				m_commConnection.close();
				s_logger.info("Closed port");
			} catch (IOException e) {
				s_logger.error("Cannot close port", e);
			}
			m_commConnection = null;
		}
	}
}
