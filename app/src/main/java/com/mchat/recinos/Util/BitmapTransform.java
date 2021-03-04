package com.mchat.recinos.Util;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.exifinterface.media.ExifInterface;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

public class BitmapTransform  {

    public static class SerialBitmap implements Serializable{
        byte[] image;
        public SerialBitmap(byte[] image){
            this.image = image;
        }
        public SerialBitmap(Bitmap image){
            this.image = toArray(image);
        }
        public byte[] getBytes(){

            return image;
        }
        public Bitmap getBitmap(){
            return toBitmap(this.image);
        }

        // Converts the Bitmap into a byte array for serialization
        public static byte[] toArray(android.graphics.Bitmap bitmap) {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 0, byteStream);
            return byteStream.toByteArray();
        }

        // Deserializes a byte array representing the Bitmap and decodes it
        public static android.graphics.Bitmap toBitmap(byte[] bitmapBytes) {
            return BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
        }

    }
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
    public static BitmapFactory.Options readMetaData(InputStream in){

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            BitmapFactory.decodeStream(in, null, options);
            in.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        return options;
    }
    public static android.graphics.Bitmap scaleDownBitmap(android.graphics.Bitmap photo, int newHeight, Context context) {

        final float densityMultiplier = context.getResources().getDisplayMetrics().density;

        int h= (int) (newHeight*densityMultiplier);
        int w= (int) (h * photo.getWidth()/((double) photo.getHeight()));

        Log.d("BITMAP_SCALE", "New Height: "+ h + " New Width: "+ w);
        Log.d("BITMAP_SCALE", "Height: "+ photo.getHeight() + " Width: "+ photo.getWidth());
        //Check that we aren't scaling up
        if(photo.getHeight() < h || photo.getWidth() < w){
            return photo;
        }
        photo= android.graphics.Bitmap.createScaledBitmap(photo, w, h, true);
        return photo;
    }
    public static android.graphics.Bitmap scaleUpBitmap(android.graphics.Bitmap photo, int newHeight, Context context) {

        final float densityMultiplier = context.getResources().getDisplayMetrics().density;

        int h= (int) (newHeight*densityMultiplier);
        int w= (int) (h * photo.getWidth()/((double) photo.getHeight()));

        Log.d("BITMAP_SCALE", "New Height: "+ h + " New Width: "+ w);
        Log.d("BITMAP_SCALE", "Height: "+ photo.getHeight() + " Width: "+ photo.getWidth());
        //May need to check if scale or not

        photo= android.graphics.Bitmap.createScaledBitmap(photo, w, h, true);
        return photo;
    }
    private static int imageRotationAngle(Context context, Uri imageUri) {
        int rotate = 0;
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            ExifInterface exif = new ExifInterface(inputStream);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
            Log.d("RotateImage", "Exif orientation: " + orientation);
            Log.d("RotateImage", "Rotate value: " + rotate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }
    public static android.graphics.Bitmap correctBitmap(Context context, android.graphics.Bitmap bitmap, Uri imageURI, int width){
        int rotation = imageRotationAngle(context, imageURI);
        if(rotation == 0)
            return  bitmap;
        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);
        android.graphics.Bitmap scaledBitmap = bitmap;
        if(width != bitmap.getWidth())
            scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, width, width, true);
        //rotated bitmap
        return android.graphics.Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

    }
    public static Bitmap compressBitmap(InputStream in, int width,int height,  int maxSizeBytes) {
        BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
        bmpFactoryOptions.inJustDecodeBounds = true;
        Bitmap bitmap;

        int heightRatio = (int)Math.ceil(bmpFactoryOptions.outHeight/(float)height);
        int widthRatio = (int)Math.ceil(bmpFactoryOptions.outWidth/(float)width);


        if (heightRatio > 1 || widthRatio > 1)
        {
            if (heightRatio > widthRatio)
            {
                bmpFactoryOptions.inSampleSize = heightRatio;
            } else {
                bmpFactoryOptions.inSampleSize = widthRatio;
            }
        }

        bmpFactoryOptions.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeStream(in, null, bmpFactoryOptions);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        int currSize;
        int currQuality = 100;
        do {
            bitmap.compress(Bitmap.CompressFormat.JPEG, currQuality, stream);
            currSize = stream.toByteArray().length;
            // limit quality by 5 percent every time
            currQuality -= 5;

        } while (currSize >= maxSizeBytes || currQuality > 0);
        byte[] imgBytes =stream.toByteArray();
        return BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.length );
    }

    public static Bitmap compressBitmap(Bitmap bitmap, Bitmap.CompressFormat compressionType, int quality){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        //Use the file extension to determine the type of compression to use
        bitmap.compress(compressionType, quality, out);
        //Update the image message
        return BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
    }
    public static void updateRoundImage(ImageView image, Resources res, byte[] imageBytes){
        //Remove the default image.
        image.setImageDrawable(null);
        RoundedBitmapDrawable dr = roundedBitmapDrawable(res, imageBytes);
        image.setBackground(dr);
    }
    public static RoundedBitmapDrawable roundedBitmapDrawable(Resources res, byte[] image){
        RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(res, BitmapTransform.SerialBitmap.toBitmap(image));
        dr.setCircular(true);
        return dr;
    }
}

