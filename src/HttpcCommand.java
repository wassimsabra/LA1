import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.TreeSet;

public class HttpcCommand {
	public String processCommand(String[] args) throws IOException {

		String cmdResponse = "";
		
		int cmdLength = args.length;
		
		if(!args[0].toLowerCase().equals("httpc")) {
			cmdResponse = "\n"+args[0]+" is unrecognised.\n";
			cmdResponse += "\nhttpc (get|post) [-v] (-h \"k:v\")* [-d inline-data] [-f file] URL\n";	
		}
		else {
			if(cmdLength == 1) {
				if(args[0].toLowerCase().equals("httpc")) {
					cmdResponse = "httpc (get|post) [-v] (-h \"k:v\")* [-d inline-data] [-f file] URL\n";
				}
			} else if (cmdLength == 2) {
				if(args[1].toLowerCase().equals("help")){
					cmdResponse = "\nhttpc is a curl-like application but supports HTTP protocol only.";
					cmdResponse = cmdResponse.concat("\nUsage:");
					cmdResponse = cmdResponse.concat("\n\thttpc command [arguments]");
					cmdResponse = cmdResponse.concat("\nThe commands are:");
					cmdResponse = cmdResponse.concat("\n\tget	executes a HTTP GET request and prints the responseLine.");
					cmdResponse = cmdResponse.concat("\n\tpost	executes a HTTP POST request and prints the responseLine.");
					cmdResponse = cmdResponse.concat("\n\thelp	prints this screen");
					cmdResponse = cmdResponse.concat("\n\nUse \"httpc help [command]\" for more information about a command.\n");
				}
			} else if (cmdLength >= 3) {
				String second = args[1].toLowerCase();
				String third = args[2].toLowerCase();
				String url = "";
				boolean verbose = false;
				//If third word is get
				if(second.equals("help") && third.equals("get")) {
					cmdResponse = "\nusage: httpc get [-v] [-h key:value] URL";
					cmdResponse = cmdResponse.concat("\nGet executes a HTTP GET request for a given URL.\n");
					cmdResponse = cmdResponse.concat("\n	-v		Prints the detail of the responseLine such as protocol, status, and headers.\n");
					cmdResponse = cmdResponse.concat("	-h key:value	Associates headers to HTTP Request with the format 'key:value'\n");						
				//If third word is post	
				} else if(second.equals("help") && third.equals("post")) {
					cmdResponse = "\nusage: httpc post [-v] [-h key:value] [-d inline-data] [-f file] URL\n";
					cmdResponse = cmdResponse.concat("\nPost executes a HTTP POST request for a given URL with inline data or from file.\n");
					cmdResponse = cmdResponse.concat("\n\t-v\t\tPrints the detail of the responseLine such as protocol, status, and headers.\n");
					cmdResponse = cmdResponse.concat("\t-h key:value\tAssociates headers to HTTP Request with the format 'key:value'.\n");
					cmdResponse = cmdResponse.concat("\t-d string\tAssociates an inline data to the body HTTP POST request.\n");
					cmdResponse = cmdResponse.concat("\t-f file\t\tAssociates the content of a file to the body HTTP POST request\n");
					cmdResponse = cmdResponse.concat("\nEither [-d] or [-f] can be used but not both.\n");
				}
				else if(second.equals("get")){
					String fileName = null;
					for (int i = 2; i < args.length; i++) {
						if(args[i].toLowerCase().equals("-v")) {
							verbose = true;
						}else if(args[i].toLowerCase().contains("http://")) {
							url=args[i].replaceAll("'", "");
						}else if(args[i].toLowerCase().equals("-o")) {
							if(i+1>args.length-1) {
								cmdResponse = "\nYou did not specify an output file name after -o, thus output was not written to file";
							}
							else {
								fileName = args[++i];
								cmdResponse="\nOutput was also written to "+fileName+"\n";
							}							
						}
					}
						(new HttpcLibrary()).getRequest(url, verbose, fileName);										
				} //get
				if(second.equals("post")) {
					TreeSet<String> headers = new TreeSet<String>();
					String data = "";
					int i =2;
					verbose = false;
					boolean isData = false;
					boolean isFile = false;
					String fileName = null;
					for (i = 2; i < args.length; i++) {
						if(args[i].toLowerCase().equals("-v")){
							verbose = true;
						}else if(args[i].toLowerCase().equals("-h")) {
							headers.add(args[++i]);
						}
						else if(args[i].toLowerCase().equals("-d") || args[i].toLowerCase().equals("--d")) {
							int quoteDelimiter = 0;
							String temp;
							isData = true;
							if(isFile) {
								cmdResponse = "Error: you can only use -d or -f but not both";
							} 
							else {
								while(quoteDelimiter<2) {
									temp = args[i+1];
									if(temp.contains("'")) {
										quoteDelimiter = temp.length() - temp.replaceAll("'", "").length();
									}
									data+=temp+" ";
									i++;
									
								}
							}
						} 
						else if (args[i].toLowerCase().equals("-f") || args[i].toLowerCase().equals("--f")) {
							isFile = true;
							if(isData) {
								cmdResponse = "\nError: you can only use -d or -f but not both\n";
							}else {
								try(BufferedReader in = new BufferedReader(new FileReader(args[i+1]))){
									StringBuilder str = new StringBuilder();
									String line = in.readLine();
									while(line !=null) {
										str.append(line);
										str.append(System.lineSeparator());
										line = in.readLine();
									}
									data = str.toString();
								}
								catch (Exception e) {
									System.out.println("\nError using file, an empty request is sent to the server\n");
								}
							}
						}else if(args[i].toLowerCase().equals("-o")) {
							fileName = args[i+1];
							cmdResponse="\nOutput was also written to "+fileName;	
						}else if(args[i].toLowerCase().contains("http://")) {
							url = args[i];
						}
					}
					if(!(isFile && isData)) {
						(new HttpcLibrary()).postRequest(url, headers, data.trim(), verbose, fileName);
					}		
				} // post
			} //larger than 3
			
		}
		
		return cmdResponse;
	}
}
