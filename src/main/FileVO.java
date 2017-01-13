package main;

public class FileVO
{
	public String name;
	public String page;
	public String ts;
	public String link;
	public String file;

	public FileVO()
	{
		name = "";
		page = "";
		ts = "";
		link = "";
		file = "";
	}
	public FileVO(String link, String file)
	{
		name = "";
		this.file = file;
		ts = "";
		this.link = link;
		page = "";
	}

}
