/*
 * Copyright (c) 2010, Leeds Metropolitan University
 *
 * This file is part of Bibliosight.
 *
 * Bibliosight is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Bibliosight is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Bibliosight. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.leedsmet.bibliosight;

/**
 *
 * @author Mike Taylor
 */
public class BibliosightClientException extends Exception {

    public BibliosightClientException()
    {
        super();
    }

    public BibliosightClientException(String message)
    {
        super(message);
    }

    public BibliosightClientException(String message, Exception cause)
    {
        super(message, cause);
    }
}
