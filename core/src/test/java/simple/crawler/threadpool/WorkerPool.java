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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class WorkerPool
{
   public static void main(String[] args) throws Exception
   {
      RejectExecutionHandlerImpl rejectHandler = new RejectExecutionHandlerImpl();
      ThreadFactory threadFactory = Executors.defaultThreadFactory();
      ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 4, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2), threadFactory, rejectHandler);
      
      MyMonitorThread monitor = new MyMonitorThread(executor, 3);
      Thread monitorThread = new Thread(monitor);
      monitorThread.start();
      for(int i = 0; i < 10; i++) {
         executor.execute(new WorkerThread("cmd" + i));
      }
      Thread.sleep(30000);
      executor.shutdown();
      Thread.sleep(5000);
      monitor.shutdown();
   }
}
