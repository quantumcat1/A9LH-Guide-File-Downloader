package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SingletonFile
{
	private Calendar calobj;
	private DateFormat df;
	private static SingletonFile inst = null;

	private SingletonFile()
	{
		df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");

	}
	public synchronized void write(String message)
	{
		BufferedWriter bw;
		try
		{
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("log.txt"), true)));
			Calendar calobj = Calendar.getInstance();

			bw.write(df.format(calobj.getTime()) + ": " + message);
	        bw.newLine();
	        bw.close();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static SingletonFile getInstance()
	{
		if(inst == null) inst = new SingletonFile();
        return inst;
    }
}
