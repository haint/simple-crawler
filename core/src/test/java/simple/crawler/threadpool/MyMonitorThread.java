/*
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package simple.crawler.threadpool;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class MyMonitorThread implements Runnable
{
   private ThreadPoolExecutor executor;
   
   private int seconds;
   
   private boolean run = true;
   
   public MyMonitorThread(ThreadPoolExecutor executor, int delay)
   {
      this.executor = executor;
      this.seconds = delay;
   }
   
   public void shutdown()
   {
      this.run = false;
   }

   public void run()
   {
      while(run) {
         System.out.println(String.format("[monitor] [%d/%d] Active: %d, Completed: %d, Task: %d, isShutdown: %s, isTerminated: %s", 
            executor.getPoolSize(),
            executor.getCorePoolSize(),
            executor.getActiveCount(),
            executor.getCompletedTaskCount(),
            executor.getTaskCount(),
            executor.isShutdown(),
            executor.isTerminated()));
         try {
            Thread.sleep(seconds * 1000);
         } catch (InterruptedException e) {
            e.printStackTrace();
         }
      }
   }
}
