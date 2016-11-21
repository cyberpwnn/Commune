package org.cyberpwn.changelog;

import org.phantomapi.clust.DataCluster;
import org.phantomapi.lang.GList;
import org.phantomapi.lang.GTime;
import org.phantomapi.util.M;

public class LogPackage
{
	private DataCluster cc;
	
	public LogPackage(DataCluster cc)
	{
		this.cc = cc;
	}
	
	public long getLatest()
	{
		long latest = -1;
		
		for(String i : cc.keys())
		{
			long date = Long.valueOf(i.split("-")[1]);
			
			if(date > latest)
			{
				latest = date;
			}
		}
		
		return latest;
	}
	
	public String lastUpdateFormatted()
	{
		return new GTime(M.ms() - getLatest()).ago();
	}
	
	public LogElement getElement(long time)
	{
		return new LogElement(time, new GList<String>(cc.getStringList("log-" + time)));
	}
	
	public GList<LogElement> getLogsFor(long after)
	{
		GList<LogElement> le = new GList<LogElement>();
		
		for(String i : cc.keys())
		{
			long date = Long.valueOf(i.split("-")[1]);
			
			if(date > after)
			{
				le.add(new LogElement(date, new GList<String>(cc.getStringList(i))));
			}
		}
		
		return le;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cc == null) ? 0 : cc.hashCode());
		
		return result;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
		{
			return true;
		}
		
		if(obj == null)
		{
			return false;
		}
		
		if(getClass() != obj.getClass())
		{
			return false;
		}
		
		LogPackage other = (LogPackage) obj;
		
		if(cc == null)
		{
			if(other.cc != null)
			{
				return false;
			}
		}
		
		else if(!cc.equals(other.cc))
		{
			return false;
		}
		
		return true;
	}
}
