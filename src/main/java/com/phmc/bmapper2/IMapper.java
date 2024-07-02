package com.phmc.bmapper2;

public interface IMapper<FROM, TO> {
    TO map(FROM from, Class<? extends TO> toClass);
}
