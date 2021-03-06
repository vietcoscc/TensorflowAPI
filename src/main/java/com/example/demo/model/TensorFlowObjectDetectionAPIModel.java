package com.example.demo.model;/* Copyright 2016 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

import org.tensorflow.Graph;
import org.tensorflow.Operation;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Vector;

/**
 * Wrapper for frozen detection models trained using the Tensorflow Object Detection API:
 * github.com/tensorflow/models/tree/master/object_detection
 */
public class TensorFlowObjectDetectionAPIModel implements Classifier {


    // Only return this many results.
    private static final int MAX_RESULTS = 100;

    // Config values.
    private String inputName;
    private int inputSize;

    // Pre-allocated buffers.
    private List<String> labels = new Vector<String>();

    private float[] outputLocations;
    private float[] outputScores;
    private float[] outputClasses;
    private float[] outputNumDetections;
    private String[] outputNames;

    private TensorFlowInferenceJavaInterface inferenceInterface;


    public static TensorFlowObjectDetectionAPIModel create(byte graphDef[], List<String> labels, int inputSize) throws Exception {


        final TensorFlowObjectDetectionAPIModel d = new TensorFlowObjectDetectionAPIModel();

        d.labels = labels;

        d.inputSize = inputSize;

        Graph graph = new Graph();

        graph.importGraphDef(graphDef);

        d.inferenceInterface = new TensorFlowInferenceJavaInterface(graphDef);

        final Graph g = d.inferenceInterface.graph();

        d.inputName = "image_tensor";

        // The inputName node has a shape of [N, H, W, C], where
        // N is the batch size
        // H = W are the height and width
        // C is the number of channels (3 for our purposes - RGB)

        final Operation inputOp = g.operation(d.inputName);
        if (inputOp == null) {
            throw new RuntimeException("Failed to find input Node '" + d.inputName + "'");
        }
        final Operation outputOp1 = g.operation("detection_scores");
        if (outputOp1 == null) {
            throw new RuntimeException("Failed to find output Node 'detection_scores'");
        }
        final Operation outputOp2 = g.operation("detection_boxes");
        if (outputOp2 == null) {
            throw new RuntimeException("Failed to find output Node 'detection_boxes'");
        }
        final Operation outputOp3 = g.operation("detection_classes");
        if (outputOp3 == null) {
            throw new RuntimeException("Failed to find output Node 'detection_classes'");
        }

        // Pre-allocate buffers.

        d.outputNames = new String[]{"detection_boxes", "detection_scores",
                "detection_classes", "num_detections"};
        d.outputScores = new float[MAX_RESULTS];
        d.outputLocations = new float[MAX_RESULTS * 4];
        d.outputClasses = new float[MAX_RESULTS];
        d.outputNumDetections = new float[1];
        return d;
    }

    private TensorFlowObjectDetectionAPIModel() {
    }

    @Override
    public List<Classifier.Recognition> recognizeImage(final byte[] byteValues) {

        inferenceInterface.feed(inputName, byteValues, 1, inputSize, inputSize, 3);
        // Run the inference call.

        inferenceInterface.run(outputNames);

        outputLocations = new float[MAX_RESULTS * 4];
        outputScores = new float[MAX_RESULTS];
        outputClasses = new float[MAX_RESULTS];
        outputNumDetections = new float[1];

        inferenceInterface.fetch(outputNames[0], outputLocations);
        inferenceInterface.fetch(outputNames[1], outputScores);
        inferenceInterface.fetch(outputNames[2], outputClasses);
        inferenceInterface.fetch(outputNames[3], outputNumDetections);


        // Find the best detections.
        final PriorityQueue<Classifier.Recognition> pq =
                new PriorityQueue<>(1, (left, right) -> {
                    // Intentionally reversed to put high confidence at the head of the queue.
                    return Float.compare(right.getConfidence(), left.getConfidence());
                });

        // Scale them back to the input size.
        for (int i = 0; i < outputScores.length; ++i) {
            final RectF detection =
                    new RectF(
                            outputLocations[4 * i + 1] * inputSize,
                            outputLocations[4 * i] * inputSize,
                            outputLocations[4 * i + 3] * inputSize,
                            outputLocations[4 * i + 2] * inputSize);
            pq.add(
                    new Classifier.Recognition("" + i, labels.get((int) outputClasses[i]), outputScores[i], detection));
        }

        final ArrayList<Classifier.Recognition> recognitions = new ArrayList<Classifier.Recognition>();
        for (int i = 0; i < Math.min(pq.size(), 20); ++i) {
            recognitions.add(pq.poll());
        }

        return recognitions;
    }


}
