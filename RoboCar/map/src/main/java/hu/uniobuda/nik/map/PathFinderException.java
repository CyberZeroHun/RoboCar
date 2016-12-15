package hu.uniobuda.nik.map;

import android.os.Build;
import android.support.annotation.RequiresApi;

/**
 * Created by Forisz on 13/12/16.
 */

public class PathFinderException extends RuntimeException {

	public PathFinderException() {
		super();
	}

	public PathFinderException(String message, Throwable cause) {
		super(message, cause);
	}

	public PathFinderException(Throwable cause) {
		super(cause);
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	protected PathFinderException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
