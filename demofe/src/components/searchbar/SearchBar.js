import React, { useState } from "react";
import "./SearchBar.css";




import { AiOutlineClose, AiOutlineSearch, AiFillDelete } from "react-icons/ai";
function SearchBar({ placeholder, data, name, choosePlace }) {
    const [filteredData, setFilteredData] = useState([]);
    const [wordEntered, setWordEntered] = useState("");

    const handleFilter = (event) => {
        const searchWord = event.target.value;
        setWordEntered(searchWord);
        const newFilter = data.filter((value) => {
         
            return value.name.toLowerCase().includes(searchWord.toLowerCase());
        });

        if (searchWord === "") {
            setFilteredData([]);
        } else {
            setFilteredData([...newFilter]);
        }
    };

    const clearInput = () => {
        setFilteredData([]);
        // setWordEntered("");
    };

    return (
        <div className="search">
            <div className="searchInputs">
                <input

                    type="text"
                    placeholder={placeholder}
                    value={wordEntered}
                    onChange={handleFilter}
                />
                <div className="searchIcon">
                    {filteredData.length === 0 ? (
                        <AiOutlineSearch />
                    ) : (
                        <AiOutlineClose id="clearBtn" onClick={clearInput} />
                    )}
                </div>
            </div>
            {filteredData.length != 0 && (
                <div className="dataResult">
                    {filteredData.slice(0, 15).map((item, key) => {
                        return (
                            <div onClick={()=>{
                                setWordEntered(item.name)
                                setFilteredData([])
                                choosePlace(item.id, name)
                               
                            }
                                } className="dataItem" id={item.id}>
                                {item.name}

                              
                            </div>
                        );
                    })}
                </div>
            )}
        </div>
    );
}

export default SearchBar;