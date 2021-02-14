package net.logicsquad.nanocaptcha.image.filter;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.Random;

/**
 * Applies a {@link RippleFilter} to the image.
 *
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public class RippleImageFilter implements ImageFilter {
	@Override
	public void filter(BufferedImage image) {
		RippleFilter filter = new RippleFilter();
		filter.setWaveType(RippleFilter.SINE);
		filter.setXAmplitude(2.6f);
		filter.setYAmplitude(1.7f);
		filter.setXWavelength(15);
		filter.setYWavelength(5);
		ImageFilter.applyFilter(image, filter);
	}

	/**
	 * A filter which distorts an image by rippling it in the X or Y directions. The
	 * amplitude and wavelength of rippling can be specified as well as whether
	 * pixels going off the edges are wrapped or not.
	 * 
	 * @author Jerry Huxtable
	 */
	private static class RippleFilter extends TransformFilter {
		/**
		 * Sine wave ripples.
		 */
		public final static int SINE = 0;

		/**
		 * Sawtooth wave ripples.
		 */
		public final static int SAWTOOTH = 1;

		/**
		 * Triangle wave ripples.
		 */
		public final static int TRIANGLE = 2;

		/**
		 * Noise ripples.
		 */
		public final static int NOISE = 3;

		private float xAmplitude, yAmplitude;
		private float xWavelength, yWavelength;
		private int waveType;

		/**
		 * Construct a RippleFilter.
		 */
		public RippleFilter() {
			xAmplitude = 5.0f;
			yAmplitude = 0.0f;
			xWavelength = yWavelength = 16.0f;
		}

		/**
		 * Set the amplitude of ripple in the X direction.
		 * 
		 * @param xAmplitude the amplitude (in pixels).
		 * @see #getXAmplitude
		 */
		public void setXAmplitude(float xAmplitude) {
			this.xAmplitude = xAmplitude;
		}

		/**
		 * Get the amplitude of ripple in the X direction.
		 * 
		 * @return the amplitude (in pixels).
		 * @see #setXAmplitude
		 */
		public float getXAmplitude() {
			return xAmplitude;
		}

		/**
		 * Set the wavelength of ripple in the X direction.
		 * 
		 * @param xWavelength the wavelength (in pixels).
		 * @see #getXWavelength
		 */
		public void setXWavelength(float xWavelength) {
			this.xWavelength = xWavelength;
		}

		/**
		 * Get the wavelength of ripple in the X direction.
		 * 
		 * @return the wavelength (in pixels).
		 * @see #setXWavelength
		 */
		public float getXWavelength() {
			return xWavelength;
		}

		/**
		 * Set the amplitude of ripple in the Y direction.
		 * 
		 * @param yAmplitude the amplitude (in pixels).
		 * @see #getYAmplitude
		 */
		public void setYAmplitude(float yAmplitude) {
			this.yAmplitude = yAmplitude;
		}

		/**
		 * Get the amplitude of ripple in the Y direction.
		 * 
		 * @return the amplitude (in pixels).
		 * @see #setYAmplitude
		 */
		public float getYAmplitude() {
			return yAmplitude;
		}

		/**
		 * Set the wavelength of ripple in the Y direction.
		 * 
		 * @param yWavelength the wavelength (in pixels).
		 * @see #getYWavelength
		 */
		public void setYWavelength(float yWavelength) {
			this.yWavelength = yWavelength;
		}

		/**
		 * Get the wavelength of ripple in the Y direction.
		 * 
		 * @return the wavelength (in pixels).
		 * @see #setYWavelength
		 */
		public float getYWavelength() {
			return yWavelength;
		}

		/**
		 * Set the wave type.
		 * 
		 * @param waveType the type.
		 * @see #getWaveType
		 */
		public void setWaveType(int waveType) {
			this.waveType = waveType;
		}

		/**
		 * Get the wave type.
		 * 
		 * @return the type.
		 * @see #setWaveType
		 */
		public int getWaveType() {
			return waveType;
		}

		protected void transformSpace(Rectangle r) {
			if (edgeAction == ZERO) {
				r.x -= (int) xAmplitude;
				r.width += (int) (2 * xAmplitude);
				r.y -= (int) yAmplitude;
				r.height += (int) (2 * yAmplitude);
			}
		}

		protected void transformInverse(int x, int y, float[] out) {
			float nx = (float) y / xWavelength;
			float ny = (float) x / yWavelength;
			float fx, fy;
			switch (waveType) {
			case SINE:
			default:
				fx = (float) Math.sin(nx);
				fy = (float) Math.sin(ny);
				break;
			case SAWTOOTH:
				fx = ImageMath.mod(nx, 1);
				fy = ImageMath.mod(ny, 1);
				break;
			case TRIANGLE:
				fx = ImageMath.triangle(nx);
				fy = ImageMath.triangle(ny);
				break;
			case NOISE:
				fx = Noise.noise1(nx);
				fy = Noise.noise1(ny);
				break;
			}
			out[0] = x + xAmplitude * fx;
			out[1] = y + yAmplitude * fy;
		}

		public String toString() {
			return "Distort/Ripple...";
		}
	}

	/**
	 * An abstract superclass for filters which distort images in some way. The
	 * subclass only needs to override two methods to provide the mapping between
	 * source and destination pixels.
	 */
	private static abstract class TransformFilter extends AbstractBufferedImageOp {

		/**
		 * Treat pixels off the edge as zero.
		 */
		public final static int ZERO = 0;

		/**
		 * Clamp pixels to the image edges.
		 */
		public final static int CLAMP = 1;

		/**
		 * Wrap pixels off the edge onto the oppsoite edge.
		 */
		public final static int WRAP = 2;

		/**
		 * Clamp pixels RGB to the image edges, but zero the alpha. This prevents gray
		 * borders on your image.
		 */
		public final static int RGB_CLAMP = 3;

		/**
		 * Use nearest-neighbout interpolation.
		 */
		public final static int NEAREST_NEIGHBOUR = 0;

		/**
		 * Use bilinear interpolation.
		 */
		public final static int BILINEAR = 1;

		/**
		 * The action to take for pixels off the image edge.
		 */
		protected int edgeAction = RGB_CLAMP;

		/**
		 * The type of interpolation to use.
		 */
		protected int interpolation = BILINEAR;

		/**
		 * The output image rectangle.
		 */
		protected Rectangle transformedSpace;

		/**
		 * The input image rectangle.
		 */
		protected Rectangle originalSpace;

		/**
		 * Set the action to perform for pixels off the edge of the image.
		 * 
		 * @param edgeAction one of ZERO, CLAMP or WRAP
		 * @see #getEdgeAction
		 */
		public void setEdgeAction(int edgeAction) {
			this.edgeAction = edgeAction;
		}

		/**
		 * Get the action to perform for pixels off the edge of the image.
		 * 
		 * @return one of ZERO, CLAMP or WRAP
		 * @see #setEdgeAction
		 */
		public int getEdgeAction() {
			return edgeAction;
		}

		/**
		 * Set the type of interpolation to perform.
		 * 
		 * @param interpolation one of NEAREST_NEIGHBOUR or BILINEAR
		 * @see #getInterpolation
		 */
		public void setInterpolation(int interpolation) {
			this.interpolation = interpolation;
		}

		/**
		 * Get the type of interpolation to perform.
		 * 
		 * @return one of NEAREST_NEIGHBOUR or BILINEAR
		 * @see #setInterpolation
		 */
		public int getInterpolation() {
			return interpolation;
		}

		/**
		 * Inverse transform a point. This method needs to be overriden by all
		 * subclasses.
		 * 
		 * @param x   the X position of the pixel in the output image
		 * @param y   the Y position of the pixel in the output image
		 * @param out the position of the pixel in the input image
		 */
		protected abstract void transformInverse(int x, int y, float[] out);

		/**
		 * Forward transform a rectangle. Used to determine the size of the output
		 * image.
		 * 
		 * @param rect the rectangle to transform
		 */
		protected void transformSpace(Rectangle rect) {
		}

		public BufferedImage filter(BufferedImage src, BufferedImage dst) {
			int width = src.getWidth();
			int height = src.getHeight();
			int type = src.getType();
			WritableRaster srcRaster = src.getRaster();

			originalSpace = new Rectangle(0, 0, width, height);
			transformedSpace = new Rectangle(0, 0, width, height);
			transformSpace(transformedSpace);

			if (dst == null) {
				ColorModel dstCM = src.getColorModel();
				dst = new BufferedImage(dstCM,
						dstCM.createCompatibleWritableRaster(transformedSpace.width, transformedSpace.height),
						dstCM.isAlphaPremultiplied(), null);
			}
			WritableRaster dstRaster = dst.getRaster();

			int[] inPixels = getRGB(src, 0, 0, width, height, null);

			if (interpolation == NEAREST_NEIGHBOUR)
				return filterPixelsNN(dst, width, height, inPixels, transformedSpace);

			int srcWidth = width;
			int srcHeight = height;
			int srcWidth1 = width - 1;
			int srcHeight1 = height - 1;
			int outWidth = transformedSpace.width;
			int outHeight = transformedSpace.height;
			int outX, outY;
			int index = 0;
			int[] outPixels = new int[outWidth];

			outX = transformedSpace.x;
			outY = transformedSpace.y;
			float[] out = new float[2];

			for (int y = 0; y < outHeight; y++) {
				for (int x = 0; x < outWidth; x++) {
					transformInverse(outX + x, outY + y, out);
					int srcX = (int) Math.floor(out[0]);
					int srcY = (int) Math.floor(out[1]);
					float xWeight = out[0] - srcX;
					float yWeight = out[1] - srcY;
					int nw, ne, sw, se;

					if (srcX >= 0 && srcX < srcWidth1 && srcY >= 0 && srcY < srcHeight1) {
						// Easy case, all corners are in the image
						int i = srcWidth * srcY + srcX;
						nw = inPixels[i];
						ne = inPixels[i + 1];
						sw = inPixels[i + srcWidth];
						se = inPixels[i + srcWidth + 1];
					} else {
						// Some of the corners are off the image
						nw = getPixel(inPixels, srcX, srcY, srcWidth, srcHeight);
						ne = getPixel(inPixels, srcX + 1, srcY, srcWidth, srcHeight);
						sw = getPixel(inPixels, srcX, srcY + 1, srcWidth, srcHeight);
						se = getPixel(inPixels, srcX + 1, srcY + 1, srcWidth, srcHeight);
					}
					outPixels[x] = ImageMath.bilinearInterpolate(xWeight, yWeight, nw, ne, sw, se);
				}
				setRGB(dst, 0, y, transformedSpace.width, 1, outPixels);
			}
			return dst;
		}

		final private int getPixel(int[] pixels, int x, int y, int width, int height) {
			if (x < 0 || x >= width || y < 0 || y >= height) {
				switch (edgeAction) {
				case ZERO:
				default:
					return 0;
				case WRAP:
					return pixels[(ImageMath.mod(y, height) * width) + ImageMath.mod(x, width)];
				case CLAMP:
					return pixels[(ImageMath.clamp(y, 0, height - 1) * width) + ImageMath.clamp(x, 0, width - 1)];
				case RGB_CLAMP:
					return pixels[(ImageMath.clamp(y, 0, height - 1) * width) + ImageMath.clamp(x, 0, width - 1)]
							& 0x00ffffff;
				}
			}
			return pixels[y * width + x];
		}

		protected BufferedImage filterPixelsNN(BufferedImage dst, int width, int height, int[] inPixels,
				Rectangle transformedSpace) {
			int srcWidth = width;
			int srcHeight = height;
			int outWidth = transformedSpace.width;
			int outHeight = transformedSpace.height;
			int outX, outY, srcX, srcY;
			int[] outPixels = new int[outWidth];

			outX = transformedSpace.x;
			outY = transformedSpace.y;
			int[] rgb = new int[4];
			float[] out = new float[2];

			for (int y = 0; y < outHeight; y++) {
				for (int x = 0; x < outWidth; x++) {
					transformInverse(outX + x, outY + y, out);
					srcX = (int) out[0];
					srcY = (int) out[1];
					// int casting rounds towards zero, so we check out[0] < 0, not srcX < 0
					if (out[0] < 0 || srcX >= srcWidth || out[1] < 0 || srcY >= srcHeight) {
						int p;
						switch (edgeAction) {
						case ZERO:
						default:
							p = 0;
							break;
						case WRAP:
							p = inPixels[(ImageMath.mod(srcY, srcHeight) * srcWidth) + ImageMath.mod(srcX, srcWidth)];
							break;
						case CLAMP:
							p = inPixels[(ImageMath.clamp(srcY, 0, srcHeight - 1) * srcWidth)
									+ ImageMath.clamp(srcX, 0, srcWidth - 1)];
							break;
						case RGB_CLAMP:
							p = inPixels[(ImageMath.clamp(srcY, 0, srcHeight - 1) * srcWidth)
									+ ImageMath.clamp(srcX, 0, srcWidth - 1)] & 0x00ffffff;
						}
						outPixels[x] = p;
					} else {
						int i = srcWidth * srcY + srcX;
						rgb[0] = inPixels[i];
						outPixels[x] = inPixels[i];
					}
				}
				setRGB(dst, 0, y, transformedSpace.width, 1, outPixels);
			}
			return dst;
		}
	}

	/**
	 * A convenience class which implements those methods of BufferedImageOp which
	 * are rarely changed.
	 */
	private static abstract class AbstractBufferedImageOp implements BufferedImageOp, Cloneable {

		public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel dstCM) {
			if (dstCM == null)
				dstCM = src.getColorModel();
			return new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(src.getWidth(), src.getHeight()),
					dstCM.isAlphaPremultiplied(), null);
		}

		public Rectangle2D getBounds2D(BufferedImage src) {
			return new Rectangle(0, 0, src.getWidth(), src.getHeight());
		}

		public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
			if (dstPt == null)
				dstPt = new Point2D.Double();
			dstPt.setLocation(srcPt.getX(), srcPt.getY());
			return dstPt;
		}

		public RenderingHints getRenderingHints() {
			return null;
		}

		/**
		 * A convenience method for getting ARGB pixels from an image. This tries to
		 * avoid the performance penalty of BufferedImage.getRGB unmanaging the image.
		 * 
		 * @param image  a BufferedImage object
		 * @param x      the left edge of the pixel block
		 * @param y      the right edge of the pixel block
		 * @param width  the width of the pixel arry
		 * @param height the height of the pixel arry
		 * @param pixels the array to hold the returned pixels. May be null.
		 * @return the pixels
		 * @see #setRGB
		 */
		public int[] getRGB(BufferedImage image, int x, int y, int width, int height, int[] pixels) {
			int type = image.getType();
			if (type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB)
				return (int[]) image.getRaster().getDataElements(x, y, width, height, pixels);
			return image.getRGB(x, y, width, height, pixels, 0, width);
		}

		/**
		 * A convenience method for setting ARGB pixels in an image. This tries to avoid
		 * the performance penalty of BufferedImage.setRGB unmanaging the image.
		 * 
		 * @param image  a BufferedImage object
		 * @param x      the left edge of the pixel block
		 * @param y      the right edge of the pixel block
		 * @param width  the width of the pixel arry
		 * @param height the height of the pixel arry
		 * @param pixels the array of pixels to set
		 * @see #getRGB
		 */
		public void setRGB(BufferedImage image, int x, int y, int width, int height, int[] pixels) {
			int type = image.getType();
			if (type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB)
				image.getRaster().setDataElements(x, y, width, height, pixels);
			else
				image.setRGB(x, y, width, height, pixels, 0, width);
		}

		public Object clone() {
			try {
				return super.clone();
			} catch (CloneNotSupportedException e) {
				return null;
			}
		}
	}

	/**
	 * A class containing static math methods useful for image processing.
	 */
	private static class ImageMath {

		/**
		 * The value of pi as a float.
		 */
		public final static float PI = (float) Math.PI;

		/**
		 * The value of half pi as a float.
		 */
		public final static float HALF_PI = (float) Math.PI / 2.0f;

		/**
		 * The value of quarter pi as a float.
		 */
		public final static float QUARTER_PI = (float) Math.PI / 4.0f;

		/**
		 * The value of two pi as a float.
		 */
		public final static float TWO_PI = (float) Math.PI * 2.0f;

		/**
		 * Apply a bias to a number in the unit interval, moving numbers towards 0 or 1
		 * according to the bias parameter.
		 * 
		 * @param a the number to bias
		 * @param b the bias parameter. 0.5 means no change, smaller values bias towards
		 *          0, larger towards 1.
		 * @return the output value
		 */
		public static float bias(float a, float b) {
//			return (float)Math.pow(a, Math.log(b) / Math.log(0.5));
			return a / ((1.0f / b - 2) * (1.0f - a) + 1);
		}

		/**
		 * A variant of the gamma function.
		 * 
		 * @param a the number to apply gain to
		 * @param b the gain parameter. 0.5 means no change, smaller values reduce gain,
		 *          larger values increase gain.
		 * @return the output value
		 */
		public static float gain(float a, float b) {
			/*
			 * float p = (float)Math.log(1.0 - b) / (float)Math.log(0.5);
			 * 
			 * if (a < .001) return 0.0f; else if (a > .999) return 1.0f; if (a < 0.5)
			 * return (float)Math.pow(2 * a, p) / 2; else return 1.0f - (float)Math.pow(2 *
			 * (1. - a), p) / 2;
			 */
			float c = (1.0f / b - 2.0f) * (1.0f - 2.0f * a);
			if (a < 0.5)
				return a / (c + 1.0f);
			else
				return (c - a) / (c - 1.0f);
		}

		/**
		 * The step function. Returns 0 below a threshold, 1 above.
		 * 
		 * @param a the threshold position
		 * @param x the input parameter
		 * @return the output value - 0 or 1
		 */
		public static float step(float a, float x) {
			return (x < a) ? 0.0f : 1.0f;
		}

		/**
		 * The pulse function. Returns 1 between two thresholds, 0 outside.
		 * 
		 * @param a the lower threshold position
		 * @param b the upper threshold position
		 * @param x the input parameter
		 * @return the output value - 0 or 1
		 */
		public static float pulse(float a, float b, float x) {
			return (x < a || x >= b) ? 0.0f : 1.0f;
		}

		/**
		 * A smoothed pulse function. A cubic function is used to smooth the step
		 * between two thresholds.
		 * 
		 * @param a1 the lower threshold position for the start of the pulse
		 * @param a2 the upper threshold position for the start of the pulse
		 * @param b1 the lower threshold position for the end of the pulse
		 * @param b2 the upper threshold position for the end of the pulse
		 * @param x  the input parameter
		 * @return the output value
		 */
		public static float smoothPulse(float a1, float a2, float b1, float b2, float x) {
			if (x < a1 || x >= b2)
				return 0;
			if (x >= a2) {
				if (x < b1)
					return 1.0f;
				x = (x - b1) / (b2 - b1);
				return 1.0f - (x * x * (3.0f - 2.0f * x));
			}
			x = (x - a1) / (a2 - a1);
			return x * x * (3.0f - 2.0f * x);
		}

		/**
		 * A smoothed step function. A cubic function is used to smooth the step between
		 * two thresholds.
		 * 
		 * @param a the lower threshold position
		 * @param b the upper threshold position
		 * @param x the input parameter
		 * @return the output value
		 */
		public static float smoothStep(float a, float b, float x) {
			if (x < a)
				return 0;
			if (x >= b)
				return 1;
			x = (x - a) / (b - a);
			return x * x * (3 - 2 * x);
		}

		/**
		 * A "circle up" function. Returns y on a unit circle given 1-x. Useful for
		 * forming bevels.
		 * 
		 * @param x the input parameter in the range 0..1
		 * @return the output value
		 */
		public static float circleUp(float x) {
			x = 1 - x;
			return (float) Math.sqrt(1 - x * x);
		}

		/**
		 * A "circle down" function. Returns 1-y on a unit circle given x. Useful for
		 * forming bevels.
		 * 
		 * @param x the input parameter in the range 0..1
		 * @return the output value
		 */
		public static float circleDown(float x) {
			return 1.0f - (float) Math.sqrt(1 - x * x);
		}

		/**
		 * Clamp a value to an interval.
		 * 
		 * @param a the lower clamp threshold
		 * @param b the upper clamp threshold
		 * @param x the input parameter
		 * @return the clamped value
		 */
		public static float clamp(float x, float a, float b) {
			return (x < a) ? a : (x > b) ? b : x;
		}

		/**
		 * Clamp a value to an interval.
		 * 
		 * @param a the lower clamp threshold
		 * @param b the upper clamp threshold
		 * @param x the input parameter
		 * @return the clamped value
		 */
		public static int clamp(int x, int a, int b) {
			return (x < a) ? a : (x > b) ? b : x;
		}

		/**
		 * Return a mod b. This differs from the % operator with respect to negative
		 * numbers.
		 * 
		 * @param a the dividend
		 * @param b the divisor
		 * @return a mod b
		 */
		public static double mod(double a, double b) {
			int n = (int) (a / b);

			a -= n * b;
			if (a < 0)
				return a + b;
			return a;
		}

		/**
		 * Return a mod b. This differs from the % operator with respect to negative
		 * numbers.
		 * 
		 * @param a the dividend
		 * @param b the divisor
		 * @return a mod b
		 */
		public static float mod(float a, float b) {
			int n = (int) (a / b);

			a -= n * b;
			if (a < 0)
				return a + b;
			return a;
		}

		/**
		 * Return a mod b. This differs from the % operator with respect to negative
		 * numbers.
		 * 
		 * @param a the dividend
		 * @param b the divisor
		 * @return a mod b
		 */
		public static int mod(int a, int b) {
			int n = a / b;

			a -= n * b;
			if (a < 0)
				return a + b;
			return a;
		}

		/**
		 * The triangle function. Returns a repeating triangle shape in the range 0..1
		 * with wavelength 1.0
		 * 
		 * @param x the input parameter
		 * @return the output value
		 */
		public static float triangle(float x) {
			float r = mod(x, 1.0f);
			return 2.0f * (r < 0.5 ? r : 1 - r);
		}

		/**
		 * Linear interpolation.
		 * 
		 * @param t the interpolation parameter
		 * @param a the lower interpolation range
		 * @param b the upper interpolation range
		 * @return the interpolated value
		 */
		public static float lerp(float t, float a, float b) {
			return a + t * (b - a);
		}

		/**
		 * Linear interpolation.
		 * 
		 * @param t the interpolation parameter
		 * @param a the lower interpolation range
		 * @param b the upper interpolation range
		 * @return the interpolated value
		 */
		public static int lerp(float t, int a, int b) {
			return (int) (a + t * (b - a));
		}

		/**
		 * Linear interpolation of ARGB values.
		 * 
		 * @param t    the interpolation parameter
		 * @param rgb1 the lower interpolation range
		 * @param rgb2 the upper interpolation range
		 * @return the interpolated value
		 */
		public static int mixColors(float t, int rgb1, int rgb2) {
			int a1 = (rgb1 >> 24) & 0xff;
			int r1 = (rgb1 >> 16) & 0xff;
			int g1 = (rgb1 >> 8) & 0xff;
			int b1 = rgb1 & 0xff;
			int a2 = (rgb2 >> 24) & 0xff;
			int r2 = (rgb2 >> 16) & 0xff;
			int g2 = (rgb2 >> 8) & 0xff;
			int b2 = rgb2 & 0xff;
			a1 = lerp(t, a1, a2);
			r1 = lerp(t, r1, r2);
			g1 = lerp(t, g1, g2);
			b1 = lerp(t, b1, b2);
			return (a1 << 24) | (r1 << 16) | (g1 << 8) | b1;
		}

		/**
		 * Bilinear interpolation of ARGB values.
		 * 
		 * @param x   the X interpolation parameter 0..1
		 * @param y   the y interpolation parameter 0..1
		 * @param rgb array of four ARGB values in the order NW, NE, SW, SE
		 * @return the interpolated value
		 */
		public static int bilinearInterpolate(float x, float y, int nw, int ne, int sw, int se) {
			float m0, m1;
			int a0 = (nw >> 24) & 0xff;
			int r0 = (nw >> 16) & 0xff;
			int g0 = (nw >> 8) & 0xff;
			int b0 = nw & 0xff;
			int a1 = (ne >> 24) & 0xff;
			int r1 = (ne >> 16) & 0xff;
			int g1 = (ne >> 8) & 0xff;
			int b1 = ne & 0xff;
			int a2 = (sw >> 24) & 0xff;
			int r2 = (sw >> 16) & 0xff;
			int g2 = (sw >> 8) & 0xff;
			int b2 = sw & 0xff;
			int a3 = (se >> 24) & 0xff;
			int r3 = (se >> 16) & 0xff;
			int g3 = (se >> 8) & 0xff;
			int b3 = se & 0xff;

			float cx = 1.0f - x;
			float cy = 1.0f - y;

			m0 = cx * a0 + x * a1;
			m1 = cx * a2 + x * a3;
			int a = (int) (cy * m0 + y * m1);

			m0 = cx * r0 + x * r1;
			m1 = cx * r2 + x * r3;
			int r = (int) (cy * m0 + y * m1);

			m0 = cx * g0 + x * g1;
			m1 = cx * g2 + x * g3;
			int g = (int) (cy * m0 + y * m1);

			m0 = cx * b0 + x * b1;
			m1 = cx * b2 + x * b3;
			int b = (int) (cy * m0 + y * m1);

			return (a << 24) | (r << 16) | (g << 8) | b;
		}

		/**
		 * Return the NTSC gray level of an RGB value.
		 * 
		 * @param rgb1 the input pixel
		 * @return the gray level (0-255)
		 */
		public static int brightnessNTSC(int rgb) {
			int r = (rgb >> 16) & 0xff;
			int g = (rgb >> 8) & 0xff;
			int b = rgb & 0xff;
			return (int) (r * 0.299f + g * 0.587f + b * 0.114f);
		}

		// Catmull-Rom splines
		private final static float m00 = -0.5f;
		private final static float m01 = 1.5f;
		private final static float m02 = -1.5f;
		private final static float m03 = 0.5f;
		private final static float m10 = 1.0f;
		private final static float m11 = -2.5f;
		private final static float m12 = 2.0f;
		private final static float m13 = -0.5f;
		private final static float m20 = -0.5f;
		private final static float m21 = 0.0f;
		private final static float m22 = 0.5f;
		private final static float m23 = 0.0f;
		private final static float m30 = 0.0f;
		private final static float m31 = 1.0f;
		private final static float m32 = 0.0f;
		private final static float m33 = 0.0f;

		/**
		 * Compute a Catmull-Rom spline.
		 * 
		 * @param x        the input parameter
		 * @param numKnots the number of knots in the spline
		 * @param knots    the array of knots
		 * @return the spline value
		 */
		public static float spline(float x, int numKnots, float[] knots) {
			int span;
			int numSpans = numKnots - 3;
			float k0, k1, k2, k3;
			float c0, c1, c2, c3;

			if (numSpans < 1)
				throw new IllegalArgumentException("Too few knots in spline");

			x = clamp(x, 0, 1) * numSpans;
			span = (int) x;
			if (span > numKnots - 4)
				span = numKnots - 4;
			x -= span;

			k0 = knots[span];
			k1 = knots[span + 1];
			k2 = knots[span + 2];
			k3 = knots[span + 3];

			c3 = m00 * k0 + m01 * k1 + m02 * k2 + m03 * k3;
			c2 = m10 * k0 + m11 * k1 + m12 * k2 + m13 * k3;
			c1 = m20 * k0 + m21 * k1 + m22 * k2 + m23 * k3;
			c0 = m30 * k0 + m31 * k1 + m32 * k2 + m33 * k3;

			return ((c3 * x + c2) * x + c1) * x + c0;
		}

		/**
		 * Compute a Catmull-Rom spline, but with variable knot spacing.
		 * 
		 * @param x        the input parameter
		 * @param numKnots the number of knots in the spline
		 * @param xknots   the array of knot x values
		 * @param yknots   the array of knot y values
		 * @return the spline value
		 */
		public static float spline(float x, int numKnots, int[] xknots, int[] yknots) {
			int span;
			int numSpans = numKnots - 3;
			float k0, k1, k2, k3;
			float c0, c1, c2, c3;

			if (numSpans < 1)
				throw new IllegalArgumentException("Too few knots in spline");

			for (span = 0; span < numSpans; span++)
				if (xknots[span + 1] > x)
					break;
			if (span > numKnots - 3)
				span = numKnots - 3;
			float t = (float) (x - xknots[span]) / (xknots[span + 1] - xknots[span]);
			span--;
			if (span < 0) {
				span = 0;
				t = 0;
			}

			k0 = yknots[span];
			k1 = yknots[span + 1];
			k2 = yknots[span + 2];
			k3 = yknots[span + 3];

			c3 = m00 * k0 + m01 * k1 + m02 * k2 + m03 * k3;
			c2 = m10 * k0 + m11 * k1 + m12 * k2 + m13 * k3;
			c1 = m20 * k0 + m21 * k1 + m22 * k2 + m23 * k3;
			c0 = m30 * k0 + m31 * k1 + m32 * k2 + m33 * k3;

			return ((c3 * t + c2) * t + c1) * t + c0;
		}

		/**
		 * Compute a Catmull-Rom spline for RGB values.
		 * 
		 * @param x        the input parameter
		 * @param numKnots the number of knots in the spline
		 * @param knots    the array of knots
		 * @return the spline value
		 */
		public static int colorSpline(float x, int numKnots, int[] knots) {
			int span;
			int numSpans = numKnots - 3;
			float k0, k1, k2, k3;
			float c0, c1, c2, c3;

			if (numSpans < 1)
				throw new IllegalArgumentException("Too few knots in spline");

			x = clamp(x, 0, 1) * numSpans;
			span = (int) x;
			if (span > numKnots - 4)
				span = numKnots - 4;
			x -= span;

			int v = 0;
			for (int i = 0; i < 4; i++) {
				int shift = i * 8;

				k0 = (knots[span] >> shift) & 0xff;
				k1 = (knots[span + 1] >> shift) & 0xff;
				k2 = (knots[span + 2] >> shift) & 0xff;
				k3 = (knots[span + 3] >> shift) & 0xff;

				c3 = m00 * k0 + m01 * k1 + m02 * k2 + m03 * k3;
				c2 = m10 * k0 + m11 * k1 + m12 * k2 + m13 * k3;
				c1 = m20 * k0 + m21 * k1 + m22 * k2 + m23 * k3;
				c0 = m30 * k0 + m31 * k1 + m32 * k2 + m33 * k3;
				int n = (int) (((c3 * x + c2) * x + c1) * x + c0);
				if (n < 0)
					n = 0;
				else if (n > 255)
					n = 255;
				v |= n << shift;
			}

			return v;
		}

		/**
		 * Compute a Catmull-Rom spline for RGB values, but with variable knot spacing.
		 * 
		 * @param x        the input parameter
		 * @param numKnots the number of knots in the spline
		 * @param xknots   the array of knot x values
		 * @param yknots   the array of knot y values
		 * @return the spline value
		 */
		public static int colorSpline(int x, int numKnots, int[] xknots, int[] yknots) {
			int span;
			int numSpans = numKnots - 3;
			float k0, k1, k2, k3;
			float c0, c1, c2, c3;

			if (numSpans < 1)
				throw new IllegalArgumentException("Too few knots in spline");

			for (span = 0; span < numSpans; span++)
				if (xknots[span + 1] > x)
					break;
			if (span > numKnots - 3)
				span = numKnots - 3;
			float t = (float) (x - xknots[span]) / (xknots[span + 1] - xknots[span]);
			span--;
			if (span < 0) {
				span = 0;
				t = 0;
			}

			int v = 0;
			for (int i = 0; i < 4; i++) {
				int shift = i * 8;

				k0 = (yknots[span] >> shift) & 0xff;
				k1 = (yknots[span + 1] >> shift) & 0xff;
				k2 = (yknots[span + 2] >> shift) & 0xff;
				k3 = (yknots[span + 3] >> shift) & 0xff;

				c3 = m00 * k0 + m01 * k1 + m02 * k2 + m03 * k3;
				c2 = m10 * k0 + m11 * k1 + m12 * k2 + m13 * k3;
				c1 = m20 * k0 + m21 * k1 + m22 * k2 + m23 * k3;
				c0 = m30 * k0 + m31 * k1 + m32 * k2 + m33 * k3;
				int n = (int) (((c3 * t + c2) * t + c1) * t + c0);
				if (n < 0)
					n = 0;
				else if (n > 255)
					n = 255;
				v |= n << shift;
			}

			return v;
		}

		/**
		 * An implementation of Fant's resampling algorithm.
		 * 
		 * @param source the source pixels
		 * @param dest   the destination pixels
		 * @param length the length of the scanline to resample
		 * @param offset the start offset into the arrays
		 * @param stride the offset between pixels in consecutive rows
		 * @param out    an array of output positions for each pixel
		 */
		public static void resample(int[] source, int[] dest, int length, int offset, int stride, float[] out) {
			int i, j;
			float sizfac;
			float inSegment;
			float outSegment;
			int a, r, g, b, nextA, nextR, nextG, nextB;
			float aSum, rSum, gSum, bSum;
			float[] in;
			int srcIndex = offset;
			int destIndex = offset;
			int lastIndex = source.length;
			int rgb;

			in = new float[length + 2];
			i = 0;
			for (j = 0; j < length; j++) {
				while (out[i + 1] < j)
					i++;
				in[j] = i + (float) (j - out[i]) / (out[i + 1] - out[i]);
//				in[j] = ImageMath.clamp( in[j], 0, length-1 );
			}
			in[length] = length;
			in[length + 1] = length;

			inSegment = 1.0f;
			outSegment = in[1];
			sizfac = outSegment;
			aSum = rSum = gSum = bSum = 0.0f;
			rgb = source[srcIndex];
			a = (rgb >> 24) & 0xff;
			r = (rgb >> 16) & 0xff;
			g = (rgb >> 8) & 0xff;
			b = rgb & 0xff;
			srcIndex += stride;
			rgb = source[srcIndex];
			nextA = (rgb >> 24) & 0xff;
			nextR = (rgb >> 16) & 0xff;
			nextG = (rgb >> 8) & 0xff;
			nextB = rgb & 0xff;
			srcIndex += stride;
			i = 1;

			while (i <= length) {
				float aIntensity = inSegment * a + (1.0f - inSegment) * nextA;
				float rIntensity = inSegment * r + (1.0f - inSegment) * nextR;
				float gIntensity = inSegment * g + (1.0f - inSegment) * nextG;
				float bIntensity = inSegment * b + (1.0f - inSegment) * nextB;
				if (inSegment < outSegment) {
					aSum += (aIntensity * inSegment);
					rSum += (rIntensity * inSegment);
					gSum += (gIntensity * inSegment);
					bSum += (bIntensity * inSegment);
					outSegment -= inSegment;
					inSegment = 1.0f;
					a = nextA;
					r = nextR;
					g = nextG;
					b = nextB;
					if (srcIndex < lastIndex)
						rgb = source[srcIndex];
					nextA = (rgb >> 24) & 0xff;
					nextR = (rgb >> 16) & 0xff;
					nextG = (rgb >> 8) & 0xff;
					nextB = rgb & 0xff;
					srcIndex += stride;
				} else {
					aSum += (aIntensity * outSegment);
					rSum += (rIntensity * outSegment);
					gSum += (gIntensity * outSegment);
					bSum += (bIntensity * outSegment);
					dest[destIndex] = ((int) Math.min(aSum / sizfac, 255) << 24)
							| ((int) Math.min(rSum / sizfac, 255) << 16) | ((int) Math.min(gSum / sizfac, 255) << 8)
							| (int) Math.min(bSum / sizfac, 255);
					destIndex += stride;
					aSum = rSum = gSum = bSum = 0.0f;
					inSegment -= outSegment;
					outSegment = in[i + 1] - in[i];
					sizfac = outSegment;
					i++;
				}
			}
		}

		/**
		 * Premultiply a block of pixels
		 */
		public static void premultiply(int[] p, int offset, int length) {
			length += offset;
			for (int i = offset; i < length; i++) {
				int rgb = p[i];
				int a = (rgb >> 24) & 0xff;
				int r = (rgb >> 16) & 0xff;
				int g = (rgb >> 8) & 0xff;
				int b = rgb & 0xff;
				float f = a * (1.0f / 255.0f);
				r *= f;
				g *= f;
				b *= f;
				p[i] = (a << 24) | (r << 16) | (g << 8) | b;
			}
		}

		/**
		 * Premultiply a block of pixels
		 */
		public static void unpremultiply(int[] p, int offset, int length) {
			length += offset;
			for (int i = offset; i < length; i++) {
				int rgb = p[i];
				int a = (rgb >> 24) & 0xff;
				int r = (rgb >> 16) & 0xff;
				int g = (rgb >> 8) & 0xff;
				int b = rgb & 0xff;
				if (a != 0 && a != 255) {
					float f = 255.0f / a;
					r *= f;
					g *= f;
					b *= f;
					if (r > 255)
						r = 255;
					if (g > 255)
						g = 255;
					if (b > 255)
						b = 255;
					p[i] = (a << 24) | (r << 16) | (g << 8) | b;
				}
			}
		}
	}

	/**
	 * Perlin Noise functions
	 */
	private static class Noise implements Function1D, Function2D, Function3D {

		private static Random randomGenerator = new Random();

		public float evaluate(float x) {
			return noise1(x);
		}

		public float evaluate(float x, float y) {
			return noise2(x, y);
		}

		public float evaluate(float x, float y, float z) {
			return noise3(x, y, z);
		}

		/**
		 * Compute turbulence using Perlin noise.
		 * 
		 * @param x       the x value
		 * @param y       the y value
		 * @param octaves number of octaves of turbulence
		 * @return turbulence value at (x,y)
		 */
		public static float turbulence2(float x, float y, float octaves) {
			float t = 0.0f;

			for (float f = 1.0f; f <= octaves; f *= 2)
				t += Math.abs(noise2(f * x, f * y)) / f;
			return t;
		}

		/**
		 * Compute turbulence using Perlin noise.
		 * 
		 * @param x       the x value
		 * @param y       the y value
		 * @param octaves number of octaves of turbulence
		 * @return turbulence value at (x,y)
		 */
		public static float turbulence3(float x, float y, float z, float octaves) {
			float t = 0.0f;

			for (float f = 1.0f; f <= octaves; f *= 2)
				t += Math.abs(noise3(f * x, f * y, f * z)) / f;
			return t;
		}

		private final static int B = 0x100;
		private final static int BM = 0xff;
		private final static int N = 0x1000;

		static int[] p = new int[B + B + 2];
		static float[][] g3 = new float[B + B + 2][3];
		static float[][] g2 = new float[B + B + 2][2];
		static float[] g1 = new float[B + B + 2];
		static boolean start = true;

		private static float sCurve(float t) {
			return t * t * (3.0f - 2.0f * t);
		}

		/**
		 * Compute 1-dimensional Perlin noise.
		 * 
		 * @param x the x value
		 * @return noise value at x in the range -1..1
		 */
		public static float noise1(float x) {
			int bx0, bx1;
			float rx0, rx1, sx, t, u, v;

			if (start) {
				start = false;
				init();
			}

			t = x + N;
			bx0 = ((int) t) & BM;
			bx1 = (bx0 + 1) & BM;
			rx0 = t - (int) t;
			rx1 = rx0 - 1.0f;

			sx = sCurve(rx0);

			u = rx0 * g1[p[bx0]];
			v = rx1 * g1[p[bx1]];
			return 2.3f * lerp(sx, u, v);
		}

		/**
		 * Compute 2-dimensional Perlin noise.
		 * 
		 * @param x the x coordinate
		 * @param y the y coordinate
		 * @return noise value at (x,y)
		 */
		public static float noise2(float x, float y) {
			int bx0, bx1, by0, by1, b00, b10, b01, b11;
			float rx0, rx1, ry0, ry1, q[], sx, sy, a, b, t, u, v;
			int i, j;

			if (start) {
				start = false;
				init();
			}

			t = x + N;
			bx0 = ((int) t) & BM;
			bx1 = (bx0 + 1) & BM;
			rx0 = t - (int) t;
			rx1 = rx0 - 1.0f;

			t = y + N;
			by0 = ((int) t) & BM;
			by1 = (by0 + 1) & BM;
			ry0 = t - (int) t;
			ry1 = ry0 - 1.0f;

			i = p[bx0];
			j = p[bx1];

			b00 = p[i + by0];
			b10 = p[j + by0];
			b01 = p[i + by1];
			b11 = p[j + by1];

			sx = sCurve(rx0);
			sy = sCurve(ry0);

			q = g2[b00];
			u = rx0 * q[0] + ry0 * q[1];
			q = g2[b10];
			v = rx1 * q[0] + ry0 * q[1];
			a = lerp(sx, u, v);

			q = g2[b01];
			u = rx0 * q[0] + ry1 * q[1];
			q = g2[b11];
			v = rx1 * q[0] + ry1 * q[1];
			b = lerp(sx, u, v);

			return 1.5f * lerp(sy, a, b);
		}

		/**
		 * Compute 3-dimensional Perlin noise.
		 * 
		 * @param x the x coordinate
		 * @param y the y coordinate
		 * @param y the y coordinate
		 * @return noise value at (x,y,z)
		 */
		public static float noise3(float x, float y, float z) {
			int bx0, bx1, by0, by1, bz0, bz1, b00, b10, b01, b11;
			float rx0, rx1, ry0, ry1, rz0, rz1, q[], sy, sz, a, b, c, d, t, u, v;
			int i, j;

			if (start) {
				start = false;
				init();
			}

			t = x + N;
			bx0 = ((int) t) & BM;
			bx1 = (bx0 + 1) & BM;
			rx0 = t - (int) t;
			rx1 = rx0 - 1.0f;

			t = y + N;
			by0 = ((int) t) & BM;
			by1 = (by0 + 1) & BM;
			ry0 = t - (int) t;
			ry1 = ry0 - 1.0f;

			t = z + N;
			bz0 = ((int) t) & BM;
			bz1 = (bz0 + 1) & BM;
			rz0 = t - (int) t;
			rz1 = rz0 - 1.0f;

			i = p[bx0];
			j = p[bx1];

			b00 = p[i + by0];
			b10 = p[j + by0];
			b01 = p[i + by1];
			b11 = p[j + by1];

			t = sCurve(rx0);
			sy = sCurve(ry0);
			sz = sCurve(rz0);

			q = g3[b00 + bz0];
			u = rx0 * q[0] + ry0 * q[1] + rz0 * q[2];
			q = g3[b10 + bz0];
			v = rx1 * q[0] + ry0 * q[1] + rz0 * q[2];
			a = lerp(t, u, v);

			q = g3[b01 + bz0];
			u = rx0 * q[0] + ry1 * q[1] + rz0 * q[2];
			q = g3[b11 + bz0];
			v = rx1 * q[0] + ry1 * q[1] + rz0 * q[2];
			b = lerp(t, u, v);

			c = lerp(sy, a, b);

			q = g3[b00 + bz1];
			u = rx0 * q[0] + ry0 * q[1] + rz1 * q[2];
			q = g3[b10 + bz1];
			v = rx1 * q[0] + ry0 * q[1] + rz1 * q[2];
			a = lerp(t, u, v);

			q = g3[b01 + bz1];
			u = rx0 * q[0] + ry1 * q[1] + rz1 * q[2];
			q = g3[b11 + bz1];
			v = rx1 * q[0] + ry1 * q[1] + rz1 * q[2];
			b = lerp(t, u, v);

			d = lerp(sy, a, b);

			return 1.5f * lerp(sz, c, d);
		}

		public static float lerp(float t, float a, float b) {
			return a + t * (b - a);
		}

		private static void normalize2(float v[]) {
			float s = (float) Math.sqrt(v[0] * v[0] + v[1] * v[1]);
			v[0] = v[0] / s;
			v[1] = v[1] / s;
		}

		static void normalize3(float v[]) {
			float s = (float) Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
			v[0] = v[0] / s;
			v[1] = v[1] / s;
			v[2] = v[2] / s;
		}

		private static int random() {
			return randomGenerator.nextInt() & 0x7fffffff;
		}

		private static void init() {
			int i, j, k;

			for (i = 0; i < B; i++) {
				p[i] = i;

				g1[i] = (float) ((random() % (B + B)) - B) / B;

				for (j = 0; j < 2; j++)
					g2[i][j] = (float) ((random() % (B + B)) - B) / B;
				normalize2(g2[i]);

				for (j = 0; j < 3; j++)
					g3[i][j] = (float) ((random() % (B + B)) - B) / B;
				normalize3(g3[i]);
			}

			for (i = B - 1; i >= 0; i--) {
				k = p[i];
				p[i] = p[j = random() % B];
				p[j] = k;
			}

			for (i = 0; i < B + 2; i++) {
				p[B + i] = p[i];
				g1[B + i] = g1[i];
				for (j = 0; j < 2; j++)
					g2[B + i][j] = g2[i][j];
				for (j = 0; j < 3; j++)
					g3[B + i][j] = g3[i][j];
			}
		}

		/**
		 * Returns the minimum and maximum of a number of random values of the given
		 * function. This is useful for making some stab at normalising the function.
		 */
		public static float[] findRange(Function1D f, float[] minmax) {
			if (minmax == null)
				minmax = new float[2];
			float min = 0, max = 0;
			// Some random numbers here...
			for (float x = -100; x < 100; x += 1.27139) {
				float n = f.evaluate(x);
				min = Math.min(min, n);
				max = Math.max(max, n);
			}
			minmax[0] = min;
			minmax[1] = max;
			return minmax;
		}

		/**
		 * Returns the minimum and maximum of a number of random values of the given
		 * function. This is useful for making some stab at normalising the function.
		 */
		public static float[] findRange(Function2D f, float[] minmax) {
			if (minmax == null)
				minmax = new float[2];
			float min = 0, max = 0;
			// Some random numbers here...
			for (float y = -100; y < 100; y += 10.35173) {
				for (float x = -100; x < 100; x += 10.77139) {
					float n = f.evaluate(x, y);
					min = Math.min(min, n);
					max = Math.max(max, n);
				}
			}
			minmax[0] = min;
			minmax[1] = max;
			return minmax;
		}

	}

	private interface Function3D {
		public float evaluate(float x, float y, float z);
	}

	private interface Function2D {
		public float evaluate(float x, float y);
	}

	private interface Function1D {
		public float evaluate(float v);
	}
}
