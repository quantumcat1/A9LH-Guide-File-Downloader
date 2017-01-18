package main;

public class FileVO
{
	public String page;
	public String name;
	public String link;
	public String file;
	public String path;
	public String region;
	public String firmware;
	public String type;
	public String message;
	public String ts;

	public FileVO()
	{
		page = "";
		name = "";
		link = "";
		file = "";
		path = "";
		region = "";
		firmware = "";
		type = "";
		message = "";
		ts = "";
	}
	public FileVO(String name, String file, String link, String page,
			String path, String region, String firmware, String type,
			String message, String ts)
	{
		this.name = name;
		this.file = file;
		this.ts = ts;
		this.link = link;
		this.page = page;
		this.path = path;
		this.region = region;
		this.firmware = firmware;
		this.type = type;
		this.message = message;
	}

}
