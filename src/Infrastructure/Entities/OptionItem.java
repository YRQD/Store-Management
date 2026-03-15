package Infrastructure.Entities;

public record OptionItem(Integer id, String label) {

    @Override
    public String toString() {
        return label;
    }
}