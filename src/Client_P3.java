import java.io.*;
import java.net.*;
import java.util.*;

public class Client_P3 {

	static String home = "C:/Users/Deepu/Desktop/Saideep_CN";
	static String location = home + "/ClientP3";
	static String chunks_location = location + "/chunks";
	ServerSocket receive_sock;
	Socket connection_socket;
	Socket client_socket;
	ObjectInputStream in_stream;
	ObjectOutputStream out_stream;
	Set<Integer> chunk_list;
	private int main_server_port;
	private int client_server_port;
	private int temp;
	private int client_neighbour_port;

	public static void main(String[] args) {
		Client_P3 cli3 = new Client_P3();	
		new File(location + "/chunks/").mkdirs();		
		String client_config = null;
		int files_to_recv;

		try {
			cli3.read_port_values();
			cli3.client_connect(cli3.main_server_port,cli3.temp);
			client_config = (String) cli3.in_stream.readObject();
			files_to_recv = (int) cli3.in_stream.readObject();
			cli3.chunk_list = Collections.synchronizedSet(new LinkedHashSet<Integer>());
			
			for (int i = 1; i <= files_to_recv; i++)
				cli3.chunk_list.add(i);
			
			int files_receive = (int) cli3.in_stream.readObject();
			while (files_receive > 0) {
				Chunk_File r_chunk_obj = cli3.receive_chunk(cli3.temp);
				if (r_chunk_obj == null)
					System.out.println("chunk received is null");
				else
					cli3.create_chunkfile(chunks_location, r_chunk_obj,cli3.temp);
				files_receive--;
			}

			cli3.client_disconnect(cli3.temp);

		String[] split_str = client_config.split(" ");
		int port = Integer.parseInt(split_str[1]);
		Thread thread =new Thread(new Runnable(){
			public void run(){
				cli3.client_server_connect(cli3.client_server_port,cli3.temp);	
		}});
		thread.start();
		

		cli3.client_connect(cli3.client_neighbour_port,cli3.temp);
		System.out.println("client3 files_to_recv:"+files_to_recv);
		while(true)
		{
			System.out.println("client3 files to download:"+cli3.chunk_list);			
			if(!cli3.chunk_list.isEmpty()){
				Integer[] a = cli3.chunk_list.toArray(new Integer[cli3.chunk_list.size()]);
				for(int i=0;i<a.length;i++){
					cli3.out_stream.writeObject(a[i]);
					cli3.out_stream.flush();
					Chunk_File r_chunk_obj = cli3.receive_chunk(cli3.temp);
					if (r_chunk_obj != null)
						cli3.create_chunkfile(chunks_location, r_chunk_obj,cli3.temp);
					}
			}
			else{
				cli3.out_stream.writeObject(-1);
				cli3.out_stream.flush();
				break;
			}
			Thread.sleep(2000);
		}
		cli3.client_disconnect(cli3.temp);
		cli3.combine_chunks(cli3.temp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void client_server_connect( int port,int val) {
		try {
			int neighbour_count = 1;
			receive_sock = new ServerSocket(port);
			System.out.println(this.getClass().getName()+"Client3-Server socket created, accepting connections...");
			while (true) {
				if (neighbour_count>0) {
					neighbour_count--;
					connection_socket = receive_sock.accept();
					System.out.println("new client connection accepted:" + connection_socket);
					new Client_serv_thread(connection_socket,chunks_location).start();

				} else {
					System.out.println("Cannot serve more clients, i am done!");
					break;
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void client_connect(int port,int val) throws InterruptedException {
		boolean b=true;
		while(b)
		{
		try {

			b=false;
			client_socket = new Socket("127.0.0.1", port);
			System.out.println(" client3 connected to :" + client_socket);
			//
			in_stream = new ObjectInputStream(client_socket.getInputStream());
			//System.out.println(" c3-3");
			out_stream = new ObjectOutputStream(client_socket.getOutputStream());
			//System.out.println(" c3-3-3");
		}
		catch(ConnectException e)
		{

			System.out.println("unable to connect to socket at: "+port+"... trying again...");
		    Thread.sleep(5000);
			b=true;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		}
	}

	public Chunk_File receive_chunk(int val) {
		Chunk_File chunk_obj = null;
		try {
			chunk_obj = (Chunk_File) in_stream.readObject();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return chunk_obj;
	}

	public void client_disconnect(int val) {
		try {
			in_stream.close();
			client_socket.close();
			System.out.println("client3 connection closed:"+client_socket);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void create_chunkfile(String chunks_location, Chunk_File r_chunk_obj,int val) {
		try {
			System.out.println("create back received chunk - " + r_chunk_obj.get_file_name());
			FileOutputStream file_out_stream = new FileOutputStream(new File(chunks_location, r_chunk_obj.get_file_name()));
			BufferedOutputStream buffer_out_stream = new BufferedOutputStream(file_out_stream);
			buffer_out_stream.write(r_chunk_obj.get_file_data(), 0, r_chunk_obj.get_chunk_size());

			chunk_list.remove(r_chunk_obj.get_file_number());

			buffer_out_stream.flush();
			buffer_out_stream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void read_port_values() throws FileNotFoundException
	{
		String my_str=null;
		
		BufferedReader buffer_reader = new BufferedReader(new FileReader(home + "/config.txt"));
		try {
			my_str = buffer_reader.readLine();
			String[] split_str = my_str.split(" ");
			main_server_port = Integer.parseInt(split_str[1]);
			
			for(int i=1 ;i<=3;i++)
			{
				my_str = buffer_reader.readLine();
			}
			split_str = my_str.split(" ");
			client_server_port = Integer.parseInt(split_str[1]);
			int client_neighbour= Integer.parseInt(split_str[2]);
			
			buffer_reader.close();
			
			BufferedReader buffer_reader_1 = new BufferedReader(new FileReader(home + "/config.txt"));
			for(int i=0;i<=client_neighbour;i++)
			{
				my_str = buffer_reader_1.readLine();
			}
			split_str = my_str.split(" ");
			 client_neighbour_port = Integer.parseInt(split_str[1]);
			 buffer_reader_1.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	public void combine_chunks(int val) {

		String chunks_location = location + "/chunks";
		File[] files = new File(chunks_location).listFiles();
		byte[] chunk = new byte[102400];
		new File(location + "/out/").mkdirs();
		try {

			Random r = new Random();
			FileOutputStream file_out_stream = new FileOutputStream(
					new File(location + "/out/" + r.nextInt(500) + files[0].getName()));
			BufferedOutputStream buffer_out_stream = new BufferedOutputStream(file_out_stream);
			for (File f : files) {
				FileInputStream file_in_stream = new FileInputStream(f);
				BufferedInputStream buffer_in_stream = new BufferedInputStream(file_in_stream);
				int bytesRead = 0;
				while ((bytesRead = buffer_in_stream.read(chunk)) > 0) {
					buffer_out_stream.write(chunk, 0, bytesRead);
				}
				file_in_stream.close();
			}
			file_out_stream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class Client_serv_thread extends Thread {

	private Socket socket;
	ObjectOutputStream out_stream;
	ObjectInputStream in_stream;
	String chunk_location;

	Client_serv_thread(Socket s, String chunk_location){
		this.socket = s;
		this.chunk_location=chunk_location;
	}

	public void run() {
		try {
			out_stream = new ObjectOutputStream(socket.getOutputStream());
			in_stream = new ObjectInputStream(socket.getInputStream());
			while(true)
			{
			int chunk_num= (int)in_stream.readObject();
			if(chunk_num<0)
				break;
			File[] files = new File(chunk_location).listFiles();
			String[] s;
			File curr_file=null;
			boolean have_file=false;
			
			int i = 0;
			int a = files.length;

			while(i < a){
				curr_file=files[i];
				s=files[i].getName().split("_");
				if(chunk_num == Integer.parseInt(s[0]))
				{
					have_file=true;
					break;		
				}
				i++;
			}

			Chunk_File s_chunk_obj;
			if(!have_file){
				s_chunk_obj = construct_chunk_obj(null, -1);
			}
			else{
				s_chunk_obj = construct_chunk_obj(curr_file, chunk_num);
			}
			
				send_chunk_obj(s_chunk_obj);
			}		
			server_disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized Chunk_File construct_chunk_obj(File file, int chunk_num) throws IOException {
		Chunk_File chunk_obj = null;
		if(!(chunk_num<=0))
		{
			byte[] chunk = new byte[102400]; 
			chunk_obj = new Chunk_File();
			System.out.println("construct chunk object to send- " + file.getName());
	
			chunk_obj.set_file_number(chunk_num);
			chunk_obj.set_file_name(file.getName());
			FileInputStream file_in_stream = new FileInputStream(file);
			BufferedInputStream buffer_in_stream = new BufferedInputStream(file_in_stream);
	
			int bytesRead = buffer_in_stream.read(chunk);
	
			chunk_obj.set_chunk_size(bytesRead);
			chunk_obj.set_file_data(chunk);
	
			buffer_in_stream.close();
			file_in_stream.close();
		}
		return chunk_obj;
	}

	public void send_chunk_obj(Chunk_File s_chunk_obj) {
		try {
			out_stream.writeObject(s_chunk_obj);
			out_stream.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void server_disconnect() {
		try {
			out_stream.close();
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
