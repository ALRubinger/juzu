package org.juzu.impl.request;

import junit.framework.TestCase;
import org.juzu.impl.spi.fs.disk.DiskFileSystem;
import org.juzu.impl.spi.fs.ram.RAMPath;
import org.juzu.test.CompilerHelper;
import org.juzu.test.Registry;
import org.juzu.test.request.MockApplication;
import org.juzu.test.request.MockClient;
import org.juzu.test.request.MockRenderBridge;
import org.juzu.test.support.Car;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ScopeTestCase extends TestCase
{

   public void testRenderScope() throws Exception
   {
      final File root = new File(System.getProperty("test.resources"));
      DiskFileSystem fs = new DiskFileSystem(root, "request", "scope", "render");

      //
      CompilerHelper<File> compiler = new CompilerHelper<File>(fs);
      compiler.assertCompile();

      //
      ClassLoader cl2 = new URLClassLoader(new URL[]{compiler.getOutput().getURL()}, Thread.currentThread().getContextClassLoader());

      //
      MockApplication<RAMPath> app = new MockApplication<RAMPath>(compiler.getOutput(), cl2);
      app.init();

      //
      MockClient client = app.client();
      MockRenderBridge render = client.render();
      assertEquals(1, render.getAttributes().size());
      long identity = Registry.<Long>unset("car");
      Car car = (Car)render.getAttributes().values().iterator().next();
      assertEquals(car.getIdentityHashCode(), identity);

      //
      client.invoke(Registry.<String>unset("action"));
      assertNull(Registry.get("car"));

      //
      client.invoke(Registry.<String>unset("resource"));
      assertNull(Registry.get("car"));
   }

   public void testFlashScope() throws Exception
   {
      final File root = new File(System.getProperty("test.resources"));
      DiskFileSystem fs = new DiskFileSystem(root, "request", "scope", "flash");

      //
      CompilerHelper<File> compiler = new CompilerHelper<File>(fs);
      compiler.assertCompile();

      //
      ClassLoader cl2 = new URLClassLoader(new URL[]{compiler.getOutput().getURL()}, Thread.currentThread().getContextClassLoader());

      //
      MockApplication<RAMPath> app = new MockApplication<RAMPath>(compiler.getOutput(), cl2);
      app.init();

      //
      MockClient client = app.client();
      MockRenderBridge render = client.render();
      long identity1 = Registry.<Long>unset("car");
      assertEquals(1, client.getFlash(1).size());
      Car car1 = (Car)client.getFlash(1).values().iterator().next();
      assertEquals(car1.getIdentityHashCode(), identity1);

      //
      client.invoke(Registry.<String>unset("action"));
      long identity2 = Registry.<Long>unset("car");
      assertNotSame(identity1, identity2);
      assertEquals(1, client.getFlash(0).size());
      Car car2 = (Car)client.getFlash(0).values().iterator().next();
      assertNotSame(car1, car2);

      //
      client.render();
      long identity3 = Registry.<Long>unset("car");
      assertEquals(identity2, identity3);
      assertEquals(1, client.getFlash(1).size());
      Car car3 = (Car)client.getFlash(1).values().iterator().next();
      assertSame(car2, car3);
   }
}