public class CWHResponse {

    private String message;
    boolean isSuccess;

    public CWHResponse(String message, boolean b)
    {
        this.message = message;
        this.isSuccess = b;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return isSuccess;
    }
}
