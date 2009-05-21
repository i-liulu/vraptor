package br.com.caelum.vraptor.interceptor.download;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import br.com.caelum.vraptor.InterceptionException;
import br.com.caelum.vraptor.Interceptor;
import br.com.caelum.vraptor.core.InterceptorStack;
import br.com.caelum.vraptor.core.MethodInfo;
import br.com.caelum.vraptor.resource.ResourceMethod;

/**
 * Intercepts methods whom return a File or an InputStream.
 *
 * @author filipesabella
 */
public class DownloadInterceptor implements Interceptor {
	private final HttpServletResponse response;
	private final MethodInfo info;

	public DownloadInterceptor(HttpServletResponse response, MethodInfo info) {
		this.response = response;
		this.info = info;
	}

	public boolean accepts(ResourceMethod method) {
		Class<?> type = method.getMethod().getReturnType();
		return InputStream.class.isAssignableFrom(type) || type == File.class || Download.class.isAssignableFrom(type);
	}

	public void intercept(InterceptorStack stack, ResourceMethod method, Object instance)
			throws InterceptionException {
    	// TODO ugly, just for now until next release
		if(!accepts(method)) {
			stack.next(method, instance);
		    return;
		}

		Object result = info.getResult();

		try {
			OutputStream output = response.getOutputStream();

			Download download = null;

			if(result instanceof InputStream) {
				InputStream input = (InputStream) result;
				download = new FileInputStreamDownload(input, null, null);
			} else if(result instanceof File) {
				File file = (File) result;
				download = new FileDownload(file, null, null);
			} else if(result instanceof Download) {
				download = (Download) result;
			}

			download.write(response);

			output.flush();
			output.close();
		} catch (IOException e) {
			throw new InterceptionException(e);
		}

	}
}
