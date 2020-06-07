import java.io.*;
import java.net.*;
import java.util.*;


//Main class who breaks the file that needs to be distributed among its client's based on config file


public class Server_P {

	ServerSocket receive_sock;
	Socket connection_socket;
	//static path where operations happen
	static String home = "C:/Users/Deepu/Desktop/Saideep_CN";
	static String chunks_location = home + "/Server";
	private int main_server_port;
	private int temp;
	static Map<Integer, ArrayList<Integer>> client_map;
	static Map<Integer, String> client_map_conf;

	public static void main(String[] args) {
		Server_P serv = new Server_P();
		try {
			serv.divide_file(serv.temp);
			serv.read_port_values(serv.temp);
			String my_str;
			
			int clients_count = 0;
			client_map_conf = new LinkedHashMap<Integer, String>();
			BufferedReader b_reader = new BufferedReader(new FileReader(home + "/config.txt"));
			b_reader.readLine();
			
			while ((my_str = b_reader.readLine()) != null)
				client_map_conf.put(++clients_count, my_str);

			String chunk_loc = chunks_location + "/chunks";
			File[] files = new File(chunk_loc).listFiles();

			int chunk_count = files.length;
			System.out.println("No. of Chunks:" + chunk_count);

			client_map = new LinkedHashMap<Integer, ArrayList<Integer>>();

			int i = 1;
			while (i <= clients_count){
				ArrayList<Integer> arr = new ArrayList<Integer>();
				int j = i;
				while(j<= chunk_count){
					arr.add(j);
					j = j + clients_count;
				}
				client_map.put(i, arr);
				i++;
			}
			System.out.println(client_map);

			if (chunk_count <= 0) {
				System.out.println("There are no files in chunks folder!");
			} else{
				serv.server_connect(files,serv.temp);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void server_connect(File[] files,int temp) {
		try {
			int client = 0; // initialized to 0
			receive_sock = new ServerSocket(main_server_port);
			System.out.println("Main Server socket created, accepting connections...");
			while (true) {
				System.out.println(client_map);
				client++;

				if (client > client_map.size()) {
					System.out.println("Cannot serve more clients, i am done!");
					break;
					
				} else {
					connection_socket = receive_sock.accept();
					System.out.println(" client connection accepted :-" + connection_socket);
					// create thread and pass the socket n files to handle
					new Server_thread(files, client_map.get(client), client_map_conf.get(client),connection_socket).start();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void divide_file(int val) {
		try {
			System.out.println("Put the testing file in Server folder and Enter the filename:");
			Console console = System.console();
			String input= console.readLine();

			File input_file = new File(chunks_location +"/"+input);
			Long file_length = input_file.length();

			System.out.println("Input File size :" + file_length);

			String new_dir = input_file.getParent() + "/chunks/";
			File out_folder = new File(new_dir);
			if (!out_folder.mkdirs())
				System.out.println("Chunks folder already exits or unable to create folder for chunks");
			else
				System.out.println("Chunks Folder created");

			byte[] chunk = new byte[102400];

			FileInputStream file_in_stream = new FileInputStream(input_file);

			BufferedInputStream buffer_stream = new BufferedInputStream(file_in_stream);
			int index = 1;
			int bytes_read;
			// chuck will be populated with data
			while ((bytes_read = buffer_stream.read(chunk)) > 0) {
				FileOutputStream file_out_stream = new FileOutputStream(
						new File(new_dir, String.format("%04d", index) + "_" + input_file.getName()));
				BufferedOutputStream buffer_out_stream = new BufferedOutputStream(file_out_stream);
				buffer_out_stream.write(chunk, 0, bytes_read);
				buffer_out_stream.close();
				index++;
			}
			buffer_stream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void read_port_values(int val) throws FileNotFoundException
	{
		String my_str=null;
		BufferedReader b_reader = new BufferedReader(new FileReader(home + "/config.txt"));
		
		try {
			my_str = b_reader.readLine();
			String[] split_str = my_str.split(" ");
			main_server_port = Integer.parseInt(split_str[1]);
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	
}

class Server_thread extends Thread {

	private Socket socket;
	File[] files;
	ObjectOutputStream out_stream;
	ArrayList<Integer> chunk_list;
	String config_client;

	Server_thread(File[] files, ArrayList<Integer> cl, String my_str, Socket s) {
		this.files = files;
		this.chunk_list = cl;
		this.config_client = my_str;
		this.socket = s;
	}

	public void run() {
		try {
			// get output stream
			out_stream = new ObjectOutputStream(socket.getOutputStream());
			out_stream.writeObject(config_client);
			out_stream.writeObject(files.length);
			out_stream.writeObject(chunk_list.size());
			Arrays.sort(files);

			int i = 0;

			while(i < chunk_list.size()){
				// construct the chunk object
				Chunk_File s_chunk_obj = chunk_obj_construct(files[chunk_list.get(i) - 1], chunk_list.get(i));
				// send file
				send_chunk(s_chunk_obj);
				// let's intro sleep
				Thread.sleep(1000);
				i++;
			}
			// disconnect
			server_disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Chunk_File chunk_obj_construct(File file, int chunk_num) throws IOException {
		byte[] chunk = new byte[102400]; // should be 100kb, see demo
		System.out.println("construct object - " + file.getName());
		
		Chunk_File chunk_obj = new Chunk_File();
		chunk_obj.set_file_number(chunk_num);
		chunk_obj.set_file_name(file.getName());

		FileInputStream file_in_stream = new FileInputStream(file);
		BufferedInputStream buffer_in_stream = new BufferedInputStream(file_in_stream);

		int bytes_read = buffer_in_stream.read(chunk);

		chunk_obj.set_chunk_size(bytes_read);
		chunk_obj.set_file_data(chunk);
		
		buffer_in_stream.close();
		file_in_stream.close();

		return chunk_obj;
	}

	public void send_chunk(Chunk_File s_chunk_obj) {
		try {

			out_stream.writeObject(s_chunk_obj);
			out_stream.flush();
			System.out.println("send object done...");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void server_disconnect() {
		try {
			out_stream.close();
			socket.close();
			System.out.println("Main Server socket closed:" + socket);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
