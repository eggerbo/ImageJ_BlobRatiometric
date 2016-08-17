import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.frame.RoiManager;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

import net.imagej.ImageJ;

import org.scijava.command.Command;
import org.scijava.plugin.Plugin;


/**
 * ImageJ 1 implementation of the blob ratiometrics in 2D images
 *
 * @author Felix Meyenhofer
 *         creation: 25.09.14
 */
@Plugin(type = Command.class, headless = true, menuPath = "Plugins > Analyze > Blob Ratiometrics (2D)")
public class BlobRatiometrics2D implements Command {

    /** Indices of the images (currently open image windows) */
    public static int ch1ImgIndex = 0;
    public static int ch2ImgIndex = 1;
    public static int maskImgIndex = 2;

    /** Plugin parameters and defaults */
    private int bgWindowSize = 40;
    private int minObjectSize = 10;
    private int maxObjectSize = Integer.MAX_VALUE;
    private boolean subtractBgCh1 = true;
    private boolean subtractBgCh2 = true;

    /** Image data */
    private ImagePlus impCh1;
    private ImagePlus impCh2;
    private ImagePlus impMask;


    /**
     * {@inheritDoc}
     */
    public void run() {

        // Get the input
        int status = showConfigurationDialog();
        if (status == 1) {
            return;
        }

        // Do some sanity checks
        if ((impCh1.getNDimensions() != impCh2.getNDimensions()) || (impCh2.getNDimensions() != impMask.getNDimensions())) {
            IJ.error("the channel and the mask need to have the same dimensions");
            return;
        }

        if ((impCh1.getStackSize() != impCh2.getStackSize()) || (impCh2.getStackSize() != impMask.getStackSize())) {
            IJ.error("channel and mask stacks need to be of the same size");
            return;
        }


        // Adjust the particle analyzer options
        String analyzeParticleOptions = "size=" + minObjectSize + "-" + maxObjectSize + " exclude clear add";
        if (impCh1.getStackSize() > 1) {
            analyzeParticleOptions += " stack";
        }

        // Create a new stack
        ImageStack newStack = new ImageStack(impCh1.getWidth(), impCh2.getHeight());

        // Create a processor array
        FloatProcessor[] fps = new FloatProcessor[impCh1.getStackSize()];
        for (int i = 0; i < impCh1.getStackSize(); i++) {
            fps[i] = new FloatProcessor(impCh1.getWidth(), impCh1.getHeight());
        }

        // Get the image processors for ch1 and ch2 to extract information
        ImageProcessor ipCh1 = impCh1.getProcessor();
        ImageProcessor ipCh2 = impCh2.getProcessor();

        // Initialize the roi manager
        RoiManager roiManager = RoiManager.getInstance();
        if (roiManager == null) {
            roiManager = new RoiManager();
        }

        // Create a new Result table
        ResultsTable resultsTable = new ResultsTable();


        // Subtract background
        if (subtractBgCh1) {
            IJ.run(impCh1, "Subtract Background...", "rolling=" + bgWindowSize);
        }
        if (subtractBgCh2) {
            IJ.run(impCh2, "Subtract Background...", "rolling=" + bgWindowSize);
        }

        //        // Smooth
        //        if (smoothCh1)
        //            IJ.run(impCh1, "Smooth", "");
        //
        //        if (smoothCh2)
        //            IJ.run(impCh2, "Smooth", "");


        // Make ROI form the mask   )
        IJ.run(impMask, "Analyze Particles...", analyzeParticleOptions);

        // Process the ROI's
        double ratio;
        int sliceNumber;
        int index;
        for (Roi roi : roiManager.getRoisAsArray()) {
            String label = roi.getName();

            // If the roi belongs to a n
            sliceNumber = roiManager.getSliceNumber(label);
            if (sliceNumber == -1) {
                sliceNumber = 1;
            }

            impCh1.setSlice(sliceNumber);
            impCh2.setSlice(sliceNumber);
            impMask.setSlice(sliceNumber);

            // Compute blob properties
            ipCh1.setRoi(roi);
            ImageStatistics statsCh1 = ipCh1.getStatistics();
            ipCh2.setRoi(roi);
            ImageStatistics statsCh2 = ipCh2.getStatistics();

            // Filter
            if ((statsCh1.area < minObjectSize) || (statsCh1.area > maxObjectSize)) {
                continue;
            }

            // Fill the ROI in the ratio image with the ratio value
            ratio = statsCh1.umean / statsCh2.umean;
            index = sliceNumber - 1;
            fps[index].setRoi(roi);
            fps[index].setValue(ratio);
            fps[index].fill(roi.getMask());


            // Write stats in the results table
            resultsTable.incrementCounter();
            resultsTable.addValue("Label", label);
            resultsTable.addValue("x centroid", statsCh1.xCentroid);
            resultsTable.addValue("y centroid", statsCh1.yCentroid);
            resultsTable.addValue("area", statsCh1.area);

            resultsTable.addValue("mean(ch1)", statsCh1.umean);
            resultsTable.addValue("mean(ch2)", statsCh2.umean);
            ratio = statsCh1.umean / statsCh2.umean;
            resultsTable.addValue("mean(ch1)/mean(ch2)", ratio);

            //			    rt.addValue("median(ch1)", statsCh1.median);
            //			    rt.addValue("median(ch2)", statsCh2.median);
            //			    ratio = (float)statsCh1.median / (float)statsCh2.median;
            //			    rt.addValue("median(ch1)/median(ch2)", ratio);

            resultsTable.addValue("mode(ch1)", statsCh1.mode);
            resultsTable.addValue("mode(ch2)", statsCh2.mode);
            ratio = (float) statsCh1.mode / (float) statsCh2.mode;
            resultsTable.addValue("mode(ch1)/mode(ch2)", ratio);

            resultsTable.addValue("max(ch1)", statsCh1.max);
            resultsTable.addValue("max(ch2)", statsCh2.max);
            ratio = statsCh1.max / statsCh2.max;
            resultsTable.addValue("max(ch1)/max(ch2)", ratio);

            resultsTable.addValue("integral(ch1)", statsCh1.area * statsCh1.mean);
            resultsTable.addValue("integral(ch2)", statsCh2.area * statsCh2.mean);
            ratio = (float) (statsCh1.area * statsCh1.mean) / (float) (statsCh2.area * statsCh2.mean);
            resultsTable.addValue("integral(ch1)/integral(ch2)", ratio);

            //IJ.log("Label: " + label + ", Slice: " + slice + ", ch1 mean: " + statsCh1.umean + ", ch2 mean: " + statsCh2.umean);
        }


        // Build a stack
        for (FloatProcessor fp : fps) {
            newStack.addSlice(fp);
        }

        // Create a image plus from the stack and display
        ImagePlus impRat = new ImagePlus("Ratio image", newStack);
        try {
//            IJ.run(impRat, "Rainbow RGB", ""); // This colormap is not in the base package ...
            IJ.run(impRat, "Fire", ""); // ... but  this one should be
        } catch (RuntimeException e) {
            IJ.log("Unable to apply the LUT 'Fire'. You will have to do this manually.");
        }

        IJ.run(impRat, "Calibration Bar...", "location=[Upper Right] fill=None label=White number=5 decimal=1 font=12 zoom=1.3 bold overlay");
        impRat.show();

        // Show the table
        resultsTable.show("Results");
    }


    /**
     * Helper method that takes care of creating a dialog and fetching all the inputs to the corresponding fields.
     *
     * @return status
     */
    private int showConfigurationDialog() {

        // Get the list of open images
        final int[] idList = WindowManager.getIDList();
        if ( idList == null || idList.length < 3 ) {
            IJ.error( "You need an open image mask an two channel images" );
            return 1;
        }

        final String[] imgList = new String[ idList.length ];
        for ( int i = 0; i < idList.length; ++i ) {
            imgList[ i ] = WindowManager.getImage(idList[i]).getTitle();
        }

        // Create
        final GenericDialog gd = new GenericDialog("Ratiometrics");
        gd.addChoice("channel 1", imgList, imgList[ch1ImgIndex]);
        gd.addChoice("channel 2", imgList, imgList[ch2ImgIndex] );
        gd.addChoice("mask", imgList, imgList[maskImgIndex] );

        gd.addCheckbox("channel 1: subtract background", true);
        gd.addCheckbox("channel 2: subtract background", true);
        gd.addNumericField("background window size:", bgWindowSize, 0);
        gd.addNumericField("min. object area:", minObjectSize, 0);
        gd.addNumericField("max. object area:", maxObjectSize, 0);

//        gd.addDialogListener(new DialogListener() {
//            @Override
//            public boolean dialogItemChanged(GenericDialog genericDialog, AWTEvent awtEvent) {
//                boolean bgFlag1 = genericDialog.getNextBoolean();
//                boolean bgFlag2 = genericDialog.getNextBoolean();
//                Vector field = genericDialog.getNumericFields();
//
//                if ((bgFlag1 || bgFlag2) && field.size() < 3)
//                    genericDialog.addNumericField("Approx. object bgWindowSize:", 80, 0);
//                return false;
//            }
//        });

        gd.showDialog();
        if (gd.wasCanceled()) {
            return 1;
        }

        impCh1 = WindowManager.getImage(idList[ch1ImgIndex = gd.getNextChoiceIndex()]);
        impCh2 = WindowManager.getImage(idList[ch2ImgIndex = gd.getNextChoiceIndex()]);
        impMask = WindowManager.getImage(idList[maskImgIndex = gd.getNextChoiceIndex()]);

        bgWindowSize = (int)gd.getNextNumber();
        minObjectSize = (int)gd.getNextNumber();
        maxObjectSize = (int)gd.getNextNumber();

        subtractBgCh1 = gd.getNextBoolean();
        subtractBgCh2 = gd.getNextBoolean();

        return 0;
    }


    /**
     * Test
     * @param args input arguments
     * @throws Exception anything that can go wrong
     */
    public static void main(final String... args) throws Exception {
        final ImageJ ij = net.imagej.Main.launch(args);
        ij.command().run(BlobRatiometrics2D.class, true);
    }

}
