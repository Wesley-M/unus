package co.unus.utils;

import org.modelmapper.ModelMapper;
import java.util.stream.Collectors;

import java.util.List;

public class ListMapper {
    private static ModelMapper modelMapper = new ModelMapper();

    public static <S, T> List<T> mapList(List<S> source, Class<T> targetClass) {
        return source.stream()
                .map(element -> modelMapper.map(element, targetClass))
                .collect(Collectors.toList());
    }
}
