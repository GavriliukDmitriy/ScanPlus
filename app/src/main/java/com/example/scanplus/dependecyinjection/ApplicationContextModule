@Module
public class ApplicationContextModule {
    
    Context context;

    public ApplicationContextModule(Context context){
        this.context = context;
    }
    
    @Named("application_context")
    @ApplicationScope
    @Provides
    public Context context(){
        return context.getApplicationContext();
    }
}
