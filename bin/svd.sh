cd ..
nohup mvn clean install exec:java -Psvd -DinputFile=training-bow -DoutputFileForU=reduced-training-bow-U -DoutputFileForS=reduced-training-bow-S -DoutputFileForV=reduced-training-bow-V
