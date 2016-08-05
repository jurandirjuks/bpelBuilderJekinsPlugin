package BpelCompiler.compiler;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.util.FormValidation;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link HelloWorldBuilder} is created. The created
 * instance is persisted to the project configuration XML by using
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform} method will be invoked. 
 *
 * @author Kohsuke Kawaguchi
 */
public class HelloWorldBuilder extends Builder implements SimpleBuildStep {

    private final String oracle_home;
    private final String java_home;
    private final String nome_projeto="NOME_PROJETO";

    public String getOracle_home() {
        return oracle_home;
    }

    public String getJava_home() {
        return java_home;
    }

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public HelloWorldBuilder(String oracle_home,String java_home) {
        this.oracle_home = oracle_home;
        this.java_home = java_home;
    }


    @Override
    public void perform(Run<?,?> build, FilePath workspace, Launcher launcher, TaskListener listener) {
        // This is where you 'build' the project.
        // Since this is a dummy, we just say 'hello world' and call that a build.

        IBpelProjectBuilder builder = new BpelProjectBuilderImpl();
        builder.build();

        try {
            listener.getLogger().println(workspace.absolutize().getName());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String vars = null;
        try {
            vars = build.getEnvironment(listener).get(nome_projeto,"default");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        listener.getLogger().println("Variavel  " + vars);


        // This also shows how you can consult the global configuration of the builder
            listener.getLogger().println("Bonjour, " + oracle_home + " "  + java_home + " nome_projeto: ");
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    /**
     * Descriptor for {@link HelloWorldBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */
        private boolean useFrench;

        /**
         * In order to load the persisted global configuration, you have to 
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        /**
         * Performs on-the-fly validation of the form field 'name'.
         *
         * @param value
         *      This parameter receives the value that the user has typed.
         * @return
         *      Indicates the outcome of the validation. This is sent to the browser.
         *      <p>
         *      Note that returning {@link FormValidation#error(String)} does not
         *      prevent the form from being saved. It just means that a message
         *      will be displayed to the user. 
         */
        public FormValidation doCheckName(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set a name");
            if (value.length() < 4)
                return FormValidation.warning("Isn't the name too short?");
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Say hello world";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            useFrench = formData.getBoolean("useFrench");
            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUseFrench)
            save();
            return super.configure(req,formData);
        }

        /**
         * This method returns true if the global configuration says we should speak French.
         *
         * The method name is bit awkward because global.jelly calls this method to determine
         * the initial state of the checkbox by the naming convention.
         */
        public boolean getUseFrench() {
            return useFrench;
        }
    }
}

