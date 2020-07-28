package com.volmit.iris;

import java.io.File;

import org.bukkit.Bukkit;

import com.volmit.iris.util.ChronoLatch;
import com.volmit.iris.util.FileWatcher;
import com.volmit.iris.util.KSet;

public class IrisHotloadManager
{
	private ChronoLatch latch;
	private KSet<FileWatcher> watchers;

	public IrisHotloadManager()
	{
		watchers = new KSet<>();
		latch = new ChronoLatch(3000);
	}

	public void check(IrisContext ch)
	{
		if(!latch.flip())
		{
			return;
		}

		Bukkit.getScheduler().scheduleSyncDelayedTask(Iris.instance, () ->
		{
			boolean modified = false;
			int c = 0;

			for(FileWatcher i : watchers)
			{
				if(i.checkModified())
				{
					c++;
					Iris.info("File Modified: " + i.getFile().getPath());
					modified = true;
				}
			}

			if(modified)
			{
				watchers.clear();
				Iris.success("Hotloading Iris (" + c + " File" + (c == 1 ? "" : "s") + " changed)");
				Iris.data.hotloaded();
				ch.onHotloaded();
			}
		});
	}

	public void track(File file)
	{
		watchers.add(new FileWatcher(file));
	}
}