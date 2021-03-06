package delco.twitter.scraping.services.implementations;

import delco.twitter.scraping.model.Images;
import delco.twitter.scraping.model.enumerations.TypeEnum;
import delco.twitter.scraping.model.twitterapi.model_content.Datum;
import delco.twitter.scraping.model.twitterapi.model_content.Includes;
import delco.twitter.scraping.model.twitterapi.model_content.Medium;
import delco.twitter.scraping.repositories.ImageRepository;
import delco.twitter.scraping.services.interfaces.ImageService;
import delco.twitter.scraping.services.interfaces.VisionAPIService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.*;

@Service
public class ImageServiceImpl implements ImageService {

    @Autowired
    private VisionAPIService visionAPIService;

    @Autowired
    private ImageRepository imageRepository;

    @SneakyThrows
    public ImageServiceImpl(){
        visionAPIService = new VisionAPIServiceImpl();
    }

    @Override
    public void addLabelsAndSaveImage(Images image) {
        imageRepository.save(image);
    }

    @Override
    public void deleteByTweetId(Long id) {
        imageRepository.deleteByTweetId(id);
    }

    /**
     * This method is used to recover the images from internet and save them in the database
     * @param include The object includes, from root, where you find the URL of the Tweet image
     * @param datum The object datum, from root, where you find the media keys of the tweet. The program
     *              search for the coincidence between the media keys and the media keys of the tweet and
     *              assign the image to the tweet.
     */
    @Override
    public List<Images> getImages(Includes include, Datum datum) {
        ArrayList<Images> images = new ArrayList<>();
        BufferedImage image = null;
        try{
            for(String mediaKey : datum.getAttachments().getMedia_keys()){
                for(Medium media : include.getMedia()){
                    if(media.getMedia_key().equals(mediaKey)){
                        try {
                            String url = media.getType().equals("photo") ? media.getUrl()
                                    : media.getPreview_image_url();

                            Images i = downloadImage(url, datum.isPossibly_sensitive());
                            if(!i.getImageObjects().isEmpty()){
                                images.add(i);
                            }
                        }  catch (NullPointerException e) {
                            System.out.println("ERROR EN LA IMAGEN "+e.getMessage());
                        }

                    }
                }
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        return images;
    }



    /**
     * This method is used to download an image from the internet. In this program, all the paths of the different
     * images comes from the Tweet element, so this method converts a ByteArrayOutputStream into an Images object
     * @param url The url to the image from twitter
     * @return Objects images to be assigned to the tweet
     */
    @Override
    public Images downloadImage(String url, boolean sensibleContent) {
        Images images = new Images();
        try{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(ensureOpaque(ImageIO.read(new URL(url))), "jpg", baos);
            images.setImage(baos.toByteArray());
            List<String> responses = visionAPIService.getValidPictureType(url);
            if(!responses.isEmpty()) {
                responses.addAll(visionAPIService.detectLabels(url));
                annotateImageWitObjects(responses, images);
                if(sensibleContent) {
                    images.setImageContent(TypeEnum.GROTESQUE);
                }else{
                    if (visionAPIService.getSafeSearch(url)) {
                        images.setImageContent(TypeEnum.GROTESQUE);
                    } else {
                        images.setImageContent(TypeEnum.KITSCH);
                    }
                }

            }
            return images;
        }  catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        return images;
    }


    private BufferedImage ensureOpaque(BufferedImage image){
        if (image.getTransparency() == BufferedImage.OPAQUE)
            return image;
        int w = image.getWidth();
        int h = image.getHeight();
        int[] pixels = new int[w * h];
        image.getRGB(0, 0, w, h, pixels, 0, w);
        BufferedImage bi2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        bi2.setRGB(0, 0, w, h, pixels, 0, w);
        return bi2;
    }



    @Override
    public void annotateImageWitObjects(List<String> responses, Images image){
        for (String res : responses) {
            image.addImageObject(res);
        }
    }

    @Override
    public void changeImageClassification(String id, String actualClassification) {
        Images image = imageRepository.findById(Long.parseLong(id)).get();
        System.out.println(actualClassification);
        System.out.println(image.toString());
        TypeEnum newClassification = actualClassification.equals("GROTESQUE") ? TypeEnum.KITSCH : TypeEnum.GROTESQUE;
        image.setImageContent(newClassification);
        System.out.println(image.toString());
        imageRepository.save(image);
    }


}

