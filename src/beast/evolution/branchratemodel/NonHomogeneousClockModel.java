package beast.evolution.branchratemodel;
import beast.core.Citation;
import beast.core.Description;
import beast.evolution.tree.Node;
import beast.core.Input;
import beast.core.Input.Validate;
import beast.core.parameter.RealParameter;

@Description("A Non homogeneous clock model")
public class NonHomogeneousClockModel extends UCRelaxedClockModel {
	public Input<RealParameter> scaleFactor = new Input<RealParameter>("scaleFactor", "The scale factor for the height.", Validate.REQUIRED);
	public Input<RealParameter> growthFactor = new Input<RealParameter>("growthFactor", "The growth factor.", Validate.REQUIRED);
	public Input<RealParameter> middle = new Input<RealParameter>("middle", "The time at the middle of the sigmoid curve.", Validate.REQUIRED);
	
	@Override
	public void initAndValidate() {
		super.initAndValidate();
		scaleFactor.get().setBounds(0.0, Double.POSITIVE_INFINITY);
	}

	@Override
	public double getRateForBranch(Node node) {
        if (node.isRoot()) {
            // root has no rate
            return 1;
        }
		
		double s = scaleFactor.get().getValue();
		double g = growthFactor.get().getValue();
		double mid = middle.get().getValue();
		
		double parentHeight = node.getParent().getHeight();
		double nodeHeight = node.getHeight();
		double heightDifference = parentHeight - nodeHeight;
		double integral;
		
		// Exponential
		// integral = s * (Math.exp( parentHeight/s ) - Math.exp( nodeHeight/s ));
		
		// Linear
		// integral = 0.5 * (parentHeight * parentHeight - nodeHeight*nodeHeight) / s + heightDifference;
		
		// Sigmoid
//		integral = s/g * (Math.log1p(Math.exp( (mid-parentHeight)/g ))-Math.log1p(Math.exp( (mid-nodeHeight)/g ))) + (s+1)*heightDifference;
		double exp_mid_g = Math.exp(mid/g);
		integral = s * g * (Math.log(exp_mid_g + Math.exp(parentHeight/g)) - Math.log(exp_mid_g + Math.exp(nodeHeight/g))) + heightDifference;
		
		double rateFactor = integral/heightDifference;		
		
		return super.getRateForBranch(node)* rateFactor;
	}
	@Override
	protected boolean requiresRecalculation() {
		if (scaleFactor.get() != null && scaleFactor.get().somethingIsDirty()) {
			return true;
		}
		if (middle.get() != null && middle.get().somethingIsDirty()) {
			return true;
		}
		if (growthFactor.get() != null && growthFactor.get().somethingIsDirty()) {
			return true;
		}

    	return super.requiresRecalculation();
    }
}