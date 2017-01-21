package main;

public class ConsoleVO
{
	public MainWindow.Firmware firmware;
	public MainWindow.Region region;
	public MainWindow.Type type;

	public ConsoleVO()
	{
		firmware = MainWindow.Firmware.ALL;
		region = MainWindow.Region.ALL;
		type = MainWindow.Type.ALL;
	}

}
