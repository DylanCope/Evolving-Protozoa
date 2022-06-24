package biology.genes;

public @interface GeneticFloatTrait {

    float value() default 0;
    float min() default Float.MIN_VALUE;
    float max() default Float.MAX_VALUE;
}
