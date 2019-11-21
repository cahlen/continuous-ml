package io.enfuse.java.ml.mlapp;

import io.enfuse.java.ml.mlapp.modelserve.JavaModelServer;
import ml.combust.mleap.core.types.StructField;
import ml.combust.mleap.runtime.frame.Row;
import ml.combust.mleap.runtime.javadsl.LeapFrameBuilder;
import ml.combust.mleap.tensor.DenseTensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class MlAppApplication {
    private final static Logger logger = LoggerFactory.getLogger(MlAppApplication.class);

    private final String TEXT = "From: mouse@thunder.mcrcim.mcgill.edu (der Mouse) Subject: Re: Creating 8 bit windows on 24 bit display.. How? Organization: McGill Research Centre for Intelligent Machines Lines: 59  In article <1993Apr16.093209.25719@fwi.uva.nl>, stolk@fwi.uva.nl (Bram) writes:  > I am using an X server that provides 3 visuals: PseudoColor 8 bit, > Truecolor 24 bit and DirectColor 24 bit.  Lucky dog... :-)  > A problem occurs when I try to create a window with a visual that is > different from the visual of the parent (which uses the default > visual which is TC24).  > In the Xlib reference guide from 'O reilly one can read in the > section about XCteateWindow, something like: >     In the current implementation of X11: When using a visual other >     than the parent's, be sure to create or find a suitable colourmap >     which is to be used in the window attributes when creating, or >     else a BadMatch occurs.  > This warning, strangely enough, is only mentioned in the newer > editions of the X11R5 guides.  It applies with equal force to earlier versions.  Presumably only recently did the author(s) decide it was important enough to mention. The necessity it refers to has always been there, but it's been implicit in the way CreateWindow requests default some attributes of the new window.  > However, even if I pass along a suitable colourmap, I still get a > BadMatch when I create a window with a non-default visual.  >   attr.colormap = cmap; >   win = XCreateWindow( [...] >           CopyFromParent,       /* border width */ >           8,                    /* depth */ >           InputOutput,          /* class */ >           vinfo.visual,         /* visual */ >           CWColormap, >           &attr >         );  This is because the warning you read is incomplete.  You have to provide not only a colormap but also a border.  The default border is CopyFromParent, which is not valid when the window's depth doesn't match its parent's.  Specify a border-pixmap of the correct depth, or a border-pixel, and the problem should go away.  There is another problem: I can't find anything to indicate that CopyFromParent makes any sense as the border_width parameter to XCreateWindow.  Your Xlib implementation probably defines CopyFromParent as zero, to simplify the conversion to wire format, so you are unwittingly asking for a border width of zero, due to the Xlib implementation not providing stricter type-checking.  (To be fair, I'm not entirely certain it's possible for Xlib to catch this.)       der Mouse      mouse@mcrcim.mcgill.edu";
    private final String TOPIC = "comp.windows.x";

    public static void main(String[] args) {
        SpringApplication.run(MlAppApplication.class, args);
    }

    @Autowired
    private JavaModelServer javaModelServer;

    @Bean
    ApplicationRunner run() {
        return args -> {
            logger.info("**** Loading model");
            javaModelServer.loadModel();

            LeapFrameBuilder leapFrameBuilder = new LeapFrameBuilder();
            List<StructField> fields = new ArrayList();
            fields.add(leapFrameBuilder.createField("text", leapFrameBuilder.createString()));
            fields.add(leapFrameBuilder.createField("topic", leapFrameBuilder.createString()));

            Row features = leapFrameBuilder.createRow(TEXT, TOPIC);

            Row result = javaModelServer.forecast(features);

            for(int i = 0 ; i < result.size(); i++){
                logger.info("***** RESULT: {}, INDEX: {}", result.get(i), i);
            }

            DenseTensor probOne = result.getTensor(5).toDense();
            DenseTensor probTwo = result.getTensor(6).toDense();
            logger.info("**** Prob 1: [{}], Probe 2: [{}]", Scala2JavaConverter.pauseCtr(probOne), Scala2JavaConverter.pauseCtr(probTwo));
        };
    }
}
