package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
			File f = new File("resources/data/" + filename + ".dat");
			if (!f.exists())
				f.createNewFile();
			FileOutputStream fileOut = new FileOutputStream(f);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(object);
			out.close();
			fileOut.close();
			System.out.println("Serialized data saved to resources/data/" + filename + ".dat" );
	    }
		catch(IOException i) {
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
		} 
		catch (FileNotFoundException e) 
		{
			File f = new File("resources/data/" + filename + ".dat");
			try {
				f.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} 
		catch (IOException|ClassNotFoundException e) {
			e.printStackTrace();
		}
		return result;
	}
}
