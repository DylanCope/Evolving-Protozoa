package utils;

import java.io.*;

public class FileIO 
{

	public static void save(Object object, String filename)
	{ 
		try
	    {
			FileOutputStream fileOut =
			new FileOutputStream(filename + ".dat");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(object);
			out.close();
			fileOut.close();
			System.out.println("Serialized data saved to:" + filename + ".dat" );
		} catch(IOException i) {
	    	i.printStackTrace();
	    }
	}
	
	public static Object load(String filename) throws IOException, ClassNotFoundException {
		FileInputStream fileIn = new FileInputStream(filename + ".dat");
		ObjectInputStream in = new ObjectInputStream(fileIn);
		Object result = in.readObject();
		in.close();
		fileIn.close();
		return result;
	}
}
