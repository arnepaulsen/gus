package remedy;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

// remedy user id  1B11Test / 

/*
 * Change Log:
 * 
 * 9/17 - always try to disconnect, even if there is an error.
 * 
 */

public class HttpSoapConnection {

	private boolean success = false;

	private boolean debug = true;

	private HttpURLConnection connection = null;

	private String url;

	String soapAction;

	public HttpSoapConnection(String url, String soapAction) {

		this.url = url;
		this.soapAction = soapAction;

		openConnection();

	}

	public boolean getSuccess() {
		// System.out.println(" http soap .. my result is : " + success);

		return success;
	}

	private void openConnection() {

		try {
			URL endpoint = new URL(url);
			connection = (HttpURLConnection) endpoint.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("SOAPAction", soapAction);
		} catch (Exception e) {
			debug("OpenConnection error : " + e.toString());
		}
	}

	public String soapSendReceive(String xml_data_str) {

		StringBuffer return_data = new StringBuffer();

		debug("SoapConnection:soapSendReceive: " + xml_data_str);

		success = false;

		try {

			OutputStreamWriter writer = new OutputStreamWriter(connection
					.getOutputStream());

			writer.write(xml_data_str, 0, xml_data_str.length());
			writer.close();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));

			String line = null;

			while ((line = reader.readLine()) != null) {
				return_data.append(line).append("\n");
			}
			reader.close();

			disconnect();

			success = true;

		} catch (Exception e) {

			try {
				System.out.println("Connection error code : "
						+ connection.getResponseCode());

				System.out.println("Error message : "
						+ connection.getResponseMessage());

				if (connection.getResponseCode() == 500) {

					System.out
							.println("Exception code 500 - Internal Server Error."
									+ e.toString());

					String errorLine = null;

					BufferedReader errors = new BufferedReader(
							new InputStreamReader(connection.getErrorStream()));

					while ((errorLine = errors.readLine()) != null) {
						return_data.append(errorLine).append("\n");
					}
					errors.close();

					System.out.println(return_data.toString());

				}
				disconnect();

			} catch (IOException ioe) {
				System.out.println("Htpp soap error : " + ioe.toString());

			}

			// write_debug(e.toString());
		}

		debug("HTTP RESULT : " + return_data.toString());

		return return_data.toString();
	}

	private void debug(String message) {
		if (debug == true) {
			System.out.println("HttpSoapConnection: " + message);
		}

	}

	private void disconnect() {

		try {
			connection.disconnect();
		} catch (Exception e) {

		}
	}

}
