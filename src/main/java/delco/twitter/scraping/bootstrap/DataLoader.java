package delco.twitter.scraping.bootstrap;

import delco.twitter.scraping.model.Sentiment;
import delco.twitter.scraping.repositories.ImageRepository;
import delco.twitter.scraping.repositories.SentimentRepository;
import delco.twitter.scraping.repositories.TweetRepository;
import delco.twitter.scraping.repositories.WordRepository;
import delco.twitter.scraping.services.implementations.ImageServiceImpl;
import delco.twitter.scraping.services.implementations.TweetServiceImpl;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.Calendar;


@Component class DataLoader implements CommandLineRunner {


    private final TweetServiceImpl tweetService;

    private final TweetRepository tweetRepository;
    private final ImageRepository imageRepository;
    private final WordRepository wordRepository;
    private final SentimentRepository sentimentRepository;
    private final ImageServiceImpl imageService;

    public DataLoader(TweetServiceImpl tweetService, TweetRepository tweetRepository, ImageRepository imageRepository, WordRepository wordRepository, SentimentRepository sentimentRepository, ImageServiceImpl imageService) {
        this.tweetService = tweetService;

        this.tweetRepository = tweetRepository;
        this.imageRepository = imageRepository;
        this.wordRepository = wordRepository;
        this.sentimentRepository = sentimentRepository;
        this.imageService = imageService;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
//            probarApiImages();
//
//            probarApiImages();
//            Date fechaLimite = new Date(2022-1900, Calendar.MARCH,15);
//            limpiarRegistros();
//            tweetService.getUserTimeline("Greenpeace", fechaLimite);
            System.out.println("Tweets cargados");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getCause() + " " + e.getMessage());
        }

    }

    public void probarApiImages(){
            imageService.detectAdult("C:\\Users\\chris\\OneDrive\\Desktop\\Accepted\\FRX9lraXIAcByDH.jpg");

            //imageService.getImageContent("C:\\Users\\chris\\OneDrive\\Desktop\\NotAccepted\\FQ4nED8UUAAdlRx.jpg");
        }


    public void limpiarRegistros(){
        tweetRepository.deleteAll();
        wordRepository.deleteAll();
        imageRepository.deleteAll();
        Iterable<Sentiment> sentiment = sentimentRepository.findAll();
        for(Sentiment s : sentiment){
            s.setAppearances(0);
            sentimentRepository.save(s);
        }
    }




}







