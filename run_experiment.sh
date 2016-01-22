#!/bin/bash
cd bin
java -cp ".:../lib/*" ch.idsia.scenarios.champ.LearningTrackBatch -i "../$1"
