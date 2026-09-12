package virtuoel.pehkui.util;

public class ModLoaderUtils
{
	public static boolean isModLoaded(final String modId)
	{
		return PehkuiPlatform.get().isModLoaded(modId);
	}
	
	public static String getLoaderName()
	{
		return PehkuiPlatform.get().getLoaderName();
	}
	
	private ModLoaderUtils()
	{
		
	}
}
