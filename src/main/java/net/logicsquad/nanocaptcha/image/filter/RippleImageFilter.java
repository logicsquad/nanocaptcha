package net.logicsquad.nanocaptcha.image.filter;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.util.Random;

/**
 * Applies a {@link RippleFilter} to the image.
 *
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @author <a href="http://www.jhlabs.com/ip/filters/">Jerry Huxtable</a>
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

	// The following code has been modified by Logic Squad, and originally carried
	// the following license:
	/*
	Copyright 2006 Jerry Huxtable

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
	*/
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
		 * Set the wavelength of ripple in the X direction.
		 * 
		 * @param xWavelength the wavelength (in pixels).
		 * @see #getXWavelength
		 */
		public void setXWavelength(float xWavelength) {
			this.xWavelength = xWavelength;
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
		 * Set the wavelength of ripple in the Y direction.
		 * 
		 * @param yWavelength the wavelength (in pixels).
		 * @see #getYWavelength
		 */
		public void setYWavelength(float yWavelength) {
			this.yWavelength = yWavelength;
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

	// The following code has been modified by Logic Squad, and originally carried
	// the following license:
	/*
	Copyright 2006 Jerry Huxtable

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
	*/
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

			transformedSpace = new Rectangle(0, 0, width, height);
			transformSpace(transformedSpace);

			if (dst == null) {
				ColorModel dstCM = src.getColorModel();
				dst = new BufferedImage(dstCM,
						dstCM.createCompatibleWritableRaster(transformedSpace.width, transformedSpace.height),
						dstCM.isAlphaPremultiplied(), null);
			}

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
						outPixels[x] = inPixels[i];
					}
				}
				setRGB(dst, 0, y, transformedSpace.width, 1, outPixels);
			}
			return dst;
		}
	}

	// The following code has been modified by Logic Squad, and originally carried
	// the following license:
	/*
	Copyright 2006 Jerry Huxtable

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
	*/
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

	// The following code has been modified by Logic Squad, and originally carried
	// the following license:
	/*
	Copyright 2006 Jerry Huxtable

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
	*/
	/**
	 * A class containing static math methods useful for image processing.
	 */
	private static class ImageMath {
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
	}

	// The following code has been modified by Logic Squad, and originally carried
	// the following license:
	/*
	Copyright 2006 Jerry Huxtable

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
	*/
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
	}

	// The following code has been modified by Logic Squad, and originally carried
	// the following license:
	/*
	Copyright 2006 Jerry Huxtable

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
	*/
	private interface Function3D {
		public float evaluate(float x, float y, float z);
	}

	// The following code has been modified by Logic Squad, and originally carried
	// the following license:
	/*
	Copyright 2006 Jerry Huxtable

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
	*/
	private interface Function2D {
		public float evaluate(float x, float y);
	}

	// The following code has been modified by Logic Squad, and originally carried
	// the following license:
	/*
	Copyright 2006 Jerry Huxtable

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
	*/
	private interface Function1D {
		public float evaluate(float v);
	}
}
