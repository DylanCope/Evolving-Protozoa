package utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class FileIO 
{

	public static void save(Object object, String filename)
	{ 
		try
	    {
			FileOutputStream fileOut =
			new FileOutputStream("resources/data/" + filename + ".dat");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(object);
			out.close();
			fileOut.close();
			System.out.println("Serialized data saved to resources/data/" + filename + ".dat" );
		} catch(IOException i) {
	    	i.printStackTrace();
	    }
	}
	
	public static Object load(String filename)
	{
		Object result = null;
		try 
		{
			FileInputStream fileIn = new FileInputStream("resources/data/" + filename + ".dat");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			result = in.readObject();
			in.close();
			fileIn.close();
		} catch (IOException|ClassNotFoundException e) {
			e.printStackTrace();
		}
		return result;
	}
}
