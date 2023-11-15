import React, { useEffect, useRef, useState } from "react";
import "./SearchBar.css";




import { AiOutlineSearch } from "react-icons/ai";
import useDetectOutsideClick from "../../hooks/useDetectOutsideClick";
function SearchBar({ placeholder, data, name, choosePlace }) {
    const [filteredData, setFilteredData] = useState([...data]);

    const [wordEntered, setWordEntered] = useState("");

    const handleFilter = (event) => {
        if (!showResult) {
            setShowResult(true)
        }

        const searchWord = event.target.value;
        setWordEntered(searchWord);
        const newFilter = data.filter((value) => {

            return value.name.toLowerCase().includes(searchWord.toLowerCase());
        });

        if (searchWord === "") {
            setFilteredData([...data]);
        } else {
            setFilteredData([...newFilter]);
        }
    };

    const clearInput = () => {
        setFilteredData([...data]);
        setWordEntered("");
        setShowResult(false)
    };

    useEffect(() => {
        setWordEntered("")
        setFilteredData([...data]);
    }, [data])



    const inputRef = useRef()
    const [showResult, setShowResult] = useDetectOutsideClick(inputRef, false, "dataItem")

    return (
        <div className="search">
            <div className="searchInputs">
                <input
                    ref={inputRef}
                    type="text"
                    placeholder={placeholder}
                    value={wordEntered}
                    onChange={handleFilter}
                    onClick={() => {
                        if (!showResult) setShowResult(true)
                    }}
                />
                <div className="searchIcon">
                    {!showResult ? (
                        <AiOutlineSearch />
                    ) : (
                        <span  id="clearBtn" className="dataItem" onClick={() => clearInput()}>X</span>
                    )}
                </div>
            </div>
            {showResult && filteredData.length != 0 && (
                <div className="dataResult">
                    {filteredData.map((item, key) => {
                        return (
                            <div onClick={() => {
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