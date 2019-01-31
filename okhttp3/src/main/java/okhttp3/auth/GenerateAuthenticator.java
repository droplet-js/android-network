package okhttp3.auth;

import okhttp3.Authenticator;
import okhttp3.Response;

public abstract class GenerateAuthenticator implements Authenticator {

    // This above code relies on this responseCount() method:
    protected final int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }
}
