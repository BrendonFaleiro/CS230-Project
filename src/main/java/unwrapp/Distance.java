package unwrapp;

class Distance{
    double bestDistance;
    int first;
    int second;

    Distance(){
        bestDistance = 0.0;
        first = 0;
        second = 0;
    }

    Distance(float bestDistance, int first, int second){
        this.bestDistance = bestDistance;
        this.first = first;
        this.second = second;
    }


}