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

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class WorkerThread implements Runnable
{
   private String command;
   
   public WorkerThread(String s)
   {
      command = s;
   }

   public void run()
   {
      System.out.println(Thread.currentThread().getName() + " Start.Command = " + command);
      processCommand();
      System.out.println(Thread.currentThread().getName() + " End");
   }
   
   private void processCommand()
   {
      try
      {
         Thread.sleep(5000);
      }
      catch(InterruptedException e)
      {
         e.printStackTrace();
      }
   }
   
   @Override
   public String toString()
   {
      return this.command;
   }
}
