//package helper;

public class Chunk_File implements java.io.Serializable {
	/*
	 *This object holds the data related to chuck, such as 
	 *its name, what its size?, sequence number?
	*/
	private static final long serialVersionUID = 1L;
	private int file_number;
	private String file_name;
	private byte[] file_data;
	private int chunk_size;
	
	public String get_file_name() {
		return file_name;
	}

	public void set_file_name(String file_name) {
		this.file_name = file_name;
	}

	public byte[] get_file_data() {
		return file_data;
	}

	public void set_file_data(byte[] file_data) {
		this.file_data = file_data;
	}

	public int get_chunk_size() {
		return chunk_size;
	}

	public void set_chunk_size(int chunk_size) {
		this.chunk_size = chunk_size;
	}

	public int get_file_number() {
		return file_number;
	}

	public void set_file_number(int file_number) {
		this.file_number = file_number;
	}

}
