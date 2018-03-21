import java.util.Scanner;

public class HttpcClient {

	public static void main(String[] args) throws Exception {
		Scanner scan = new Scanner(System.in);
		boolean keepGoing = true;
		String command;
		HttpcCommand cmd = new HttpcCommand();
		while(keepGoing) {
			System.out.print("[HTTP-CLIENT]> ");
			command = scan.nextLine();
			String response = cmd.processCommand(command.split(" "));
			System.out.println(response);
		}
		scan.close();
	}
}

	