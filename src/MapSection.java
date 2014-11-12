import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;
import java.awt.image.RenderedImage;
import java.awt.image.ShortLookupTable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.TimerTask;

import javax.imageio.ImageIO;

import com.jhlabs.image.DilateFilter;
import com.jhlabs.image.ErodeFilter;
import com.jhlabs.image.ReduceNoiseFilter;


public class MapSection {
	
	// https://maps.googleapis.com/maps/api/staticmap?center=40.714728,-73.998672&zoom=18&size=6000x6000
	private BufferedImage imageSection;
	
	public BufferedImage getImageSection() {
		return imageSection;
	}

	public MapSection(int toleranceMax, int toleranceMin) throws IOException{
		URL nonLabelurl = new URL("https://maps.googleapis.com/maps/api/staticmap?center=40.714728,-73.998672&zoom=18&size=6000x6000&style=feature:all|element:labels|visibility:off&key=AIzaSyAeiTCYdp5wB-m9fJskfzKQBW4SWefHyEs");
		imageSection = ImageIO.read(nonLabelurl);
		imageSection = processImagePixally(imageSection, toleranceMax, toleranceMin);
		//writeImage(imageSection, "Max_" + toleranceMax + " Min_" + toleranceMin);
	}
	
	private static void writeImage(RenderedImage ri, String name) throws IOException{
		new File("DebugImage").mkdirs();
		ImageIO.write(ri, "png", new File("DebugImage" + File.separator + name + ".png"));
	}
	
	private static BufferedImage processImagePixally(final BufferedImage image, int toleranceMax, int toleranceMin){
		BufferedImage img = new BufferedImage(
			    image.getWidth(), 
			    image.getHeight(), 
			    BufferedImage.TYPE_INT_ARGB);
		ColorConvertOp cco = new ColorConvertOp(null);
		cco.filter(image, img);
		
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				int px = img.getRGB(x, y);
				int alpha = (px >> 24) & 0xFF;
				int red = (px >> 16) & 0xFF;
				int green = (px >> 8) & 0xFF;
				int blue = px & 0xFF;
			    // do stuff here
				// different color for tops of buildings
				
				if(red == 242 && green == 240 && blue == 233){
					//red = blue = green = 255;
					blue = 255;
					green = red = 0;
				}
				else if(red == 196 && green == 196 && blue == 212){
					// fair game, magic unformulaic value for dark zones
				}
				else{
					// the trick here is to limit it to a particular band of grey
					int diff = 0;
					// could optimize
					int[] diffCandidates = {red, green, blue};
					Arrays.sort(diffCandidates); 
					diff = diffCandidates[2] - diffCandidates[0];
					
					if(toleranceMax >= diff && diff >= toleranceMin){
						// should be fair game
						
					}
					else{
						red = blue = green = 0;
					}
				}
				
				
				// end do stuff
				int pixel = (alpha<<24) + (red<<16) + (green<<8) + blue;
		        img.setRGB(x, y, pixel);
		    }
		}
		
		DilateFilter df = new DilateFilter();
		img = df.filter(img, null);
		
		ErodeFilter ef = new ErodeFilter();
		img = ef.filter(img, null);
		
		return img;
	}
	
	private static BufferedImage processImage(final BufferedImage image){
		// init lookup table to zero
		BufferedImage finalImage = new BufferedImage(
			    image.getWidth(), 
			    image.getHeight(), 
			    BufferedImage.TYPE_INT_RGB);
		ColorConvertOp cco = new ColorConvertOp(null);
		cco.filter(image, finalImage);
		
		short[] red = new short[256];
		short[] green = new short[256];
		short[] blue = new short[256];
		
		// set to default values
		red[242] = 255;
		green[240] = 255;
		blue[233] = 255;
		
		// hold all data for table
		short[][] data = new short[][]{
				red, green, blue
		};
		LookupTable lookupTable = new ShortLookupTable(0, data);
		LookupOp op = new LookupOp(lookupTable, null);
		op.filter(finalImage, finalImage);
		return finalImage;
	}
}
