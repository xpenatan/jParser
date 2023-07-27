import org.teavm.classlib.ResourceSupplier;
import org.teavm.classlib.ResourceSupplierContext;

public class LibResourcesSupplier implements ResourceSupplier {
    @Override
    public String[] supplyResources(ResourceSupplierContext context) {
        String[] result = { "exampleLib.js" };
        return result;
    }
}