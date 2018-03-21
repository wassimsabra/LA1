import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.TreeSet;

public class HttpcLibrary {
	/**
	 * Recursive method that read a new line from the buffer, accumulate an output string, and return it
	 * @param in the the socket buffer reader (input)
	 * @param responseLine one more line read from the socket reponse
	 * @param contentLength is the length of the actual page content (excluding headers)
	 * @param output accumulate line by line the responseLine content
	 * @param headersProcessed set to true when a new empty line is reached, that means all headers are parse, the following is the actual page content
	 * @return Socket responseLine string
	 */
	public String parseContent(BufferedReader in, String responseLine, int contentLength, String output, Boolean headersProcessed) {
		
			try {
				    responseLine = in.readLine();
				    	if(headersProcessed && responseLine != null) {
				    		contentLength -= 1+responseLine.length();
				    	}
				    	if(responseLine == null) {
				    		return output;
				    	}
				    	if(headersProcessed && contentLength == 0) {
				    		return output.concat(responseLine);
				    	}
				    	else if(responseLine != null){
				    		if(responseLine.isEmpty() && !headersProcessed) {
				    		headersProcessed = true;
				    	}
				    	else if(!responseLine.isEmpty()){
				    		if(responseLine.toLowerCase().contains("content-length") && !headersProcessed) {
				    			contentLength = Integer.parseInt(responseLine.split(":")[1].trim());
				    		}
						}
				    	return parseContent(in, responseLine, contentLength, output.concat(responseLine+"\n"), headersProcessed);			    	
				    	}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;		
	}
	
	public void getRequest(String url, boolean verbose, String fileName){
		URL uri = null;
		try {
			uri = new URL(url.replaceAll("'", ""));
			int port;
			
			String host = uri.getHost();
			String query = uri.getQuery();
			String request;
			if(uri.getPath().equals("")) {
				request = "GET /";
			}
			else {
				request = "GET "+uri.getPath();
			}
			
			if(query != null) {
				request = request.concat("?"+query+" HTTP/1.1\r\n");
			}
			else{
				request = request.concat(" HTTP/1.1\r\n");
			}
			request += "Host: "+host+"\r\n\r\n";
			port = uri.getPort();
			if(uri.getPort()<1) {
				port = 80;
			}
			processSocketRequest(host, port, request, verbose, fileName);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void postRequest(String url, TreeSet<String> headers, String data, boolean verbose, String fileName) throws UnknownHostException, IOException {
		data = tokenise(data);
		int dataLength = data.length();
		URL uri = null;
		try {
			uri = new URL(url);
			int port;
			port = uri.getPort();
			if(uri.getPort()<1) {
				port = 80;
			}
			String host = uri.getHost();
			String path = uri.getPath();
			if(path.equals("")) {
				path = "/";
			}
			String request = "POST "+path+" HTTP/1.1\r\n";
			request+="Host: "+host+"\r\n";
			String[] temp;
			for (String header : headers) {
				temp = header.split(":");
				request+=temp[0]+": "+temp[1]+"\r\n";
			}
			request+="Content-Length: "+dataLength+"\r\n";
			request+="\r\n";
			request+=data;
			processSocketRequest(host, port, request, verbose, fileName);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String tokenise(String data) {
		data = data.replaceAll("'", "");
		return data;
	}

	private void processSocketRequest(String host, int port, String request, Boolean verbose, String fileName) {
		try{
			//Create a new Socket
			Socket s = new Socket(host, port);
			//Writer to send the request, auto Flush set to true
			PrintWriter out = new PrintWriter(s.getOutputStream(), true);
			out.println(request); //Write
			//Process and print the response
			getResponse(s,verbose, fileName);
			s.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private void getResponse(Socket socket, Boolean verbose, String fileName) throws IOException {
		//Buffered Reader to read the responseLine
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		String response = parseContent(in, "", 0, "", false);
		String [] parsedContent = response.split("\n\n");
		String responseCode = (parsedContent[0].split(" "))[1];
		String output = "";
		if(responseCode.charAt(0) == '3') {
			Socket s = new Socket();
			String request = "GET /status/418 HTTP/1.1\r\n";
			request+="Host: httpbin.org\r\n\r\n";
			System.out.println("\nResponse code 3xx was return, \nyour request was redirected to httpbin.org/status/418\n");
			processSocketRequest("httpbin.org", 80, request, verbose, fileName);
			s.close();
		}		
		else if(verbose) {
			for (int i = 0; i < parsedContent.length; i++) {
				output+=parsedContent[i]+"\n";
			}
		}
		else {
			if(parsedContent.length > 1) {
					if(parsedContent[0].equals("")) {
						output=("\nEmpty Response!");
					}
					else {
						for (int i = 1; i < parsedContent.length; i++) {
							output+="\n"+parsedContent[i];
						}
					}
			}
			else {
				output=("\nNo Response");
			}
				
		}
		if(fileName != null) {
			if(!output.isEmpty()) {
				try {
					Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "utf-8"));				
					writer.write(output);
					writer.close();
				}catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}
		}
		System.out.println(output);
		in.close();
	}
	
}
