import React, { useEffect, useState } from "react";

import { Marker, Polyline } from "react-leaflet";
import { deleteAPI } from "./fetchData";
import { useRef } from "react";
import Map from "./Map";
import "leaflet/dist/leaflet.css";
import "./App.css";
import { greenIcon } from "./components/leaftlet/Icon";
import SearchBar from "./components/searchbar/SearchBar";
import SelectDropDown from "./components/dropdown/SelectDropDown"
import Dialog from "./components/dialog/Dialog";


import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import Loading from "./components/loading/Loading";
function App() {
  const [center, setCenter] = useState({ lat: "", lng: "" });
  const mapRef = useRef();
  const [searchData, setSearchData] = useState(null);
  const [path, setPath] = useState([]);

  const [activeMap, setActiveMap] = useState("");
  const [mapList, setMapList] = useState([]);
  const [places, setPlaces] = useState([]);
  const [appName, setAppName] = useState("abc");
  const onInputChange = (e) => {
    const { name, value } = e.target;
    setSearchData({
      ...searchData,
      [name]: value,
    });
  };

  const fetchMaps = async () => {
    const rs = await fetch("http://localhost:8080/maps");
    const rsdata = await rs.json();
    setMapList(rsdata);
    setActiveMap(rsdata[0]);
  };

  const fetchCurMap = async () => {
    if (activeMap) {
      const rs = await fetch(`http://localhost:8080/map/${activeMap}`);
      const resdata = await rs.json();
      setCenter({ lng: resdata.lng, lat: resdata.lat });
      const rs1 = await fetch(`http://localhost:8080/places`);
      const resdata1 = await rs1.json();
      setPlaces(resdata1);
    }
  };

  useEffect(() => {
    fetchMaps();
  }, []);
  useEffect(() => {
    fetchCurMap();
  }, [activeMap]);

  const [selectedFile, setSelectedFile] = useState(null);
  const onChangeFile = (e) => {
    setSelectedFile(e.target.files[0]);
  };
  const uploadFile = async () => {
    if (selectedFile) {
      const formData = new FormData();

      formData.append("file", selectedFile);

      const rs = await fetch("http://localhost:8080/add-new-map", {
        method: "POST",
        body: formData,
      });
      const rsdata = await rs.json();
      console.log(rsdata);
    }
  };

  const searchSubmit = async () => {
    if (!findData.from || !findData.to) {
      return;
    }
    const resdata = await fetch(
      `http://localhost:8080/search?source=${findData.from}&destination=${findData.to}`
    )
      .then((response) => {
        if (!response.ok) {
          throw new Error("Có lỗi khi tải dữ liệu từ API");
        }

        return response.json();
      })

      .catch((error) => {
        console.error(error);
      });

    console.log(resdata);
    setPath(resdata);
  };



  const [findData, setFindData] = useState({ from: "", to: "" });

  const choosePlace = (placeId, name) => {
    setFindData({
      ...findData,
      [name]: placeId,
    });
  };

  const [dialogData, setDialogData] = useState({
    isOpen: false,
    data: ''
  })

  const changeDialogDataIsOpen = (isOpen) => {
    setDialogData({ ...dialogData, isOpen })
  }

  const handleClickDelete = (toDeleteMapName) => {
    setDialogData({
      isOpen: true,
      data: toDeleteMapName
    })
  }


  const deleteService = async () => {
    if (dialogData.data) {
      const rs = await deleteAPI(`http://localhost:8080/map/${dialogData.data}`);
      toast.info("Delete success")
      setMapList([...mapList].filter(el => el != dialogData.data))
    }
  }
  return (
    <div>
      <Loading></Loading>
      <ToastContainer />

      {
        dialogData.isOpen && <Dialog
          title={`Wanna delete map ${dialogData.data}?`}
          showButtons={true}
          firstButtonText="OK"
          secondButtonText="CANCEL"
          firstBtnHandler={() => {
            deleteService()
          }}
          secondBtnHandler={() => {
           

            changeDialogDataIsOpen(false)
          }} />
      }

      <div className="header">
        <div className="left">
          <SearchBar
            name="from"
            choosePlace={choosePlace}
            data={places}
          ></SearchBar>

          <SearchBar
            name="to"
            choosePlace={choosePlace}
            data={places}
          ></SearchBar>

          <button onClick={searchSubmit}>find</button>
        </div>

        <div className="right">


          <SelectDropDown value={activeMap} data={mapList} handleOnClick={(el) => setActiveMap(el)}


            handleClickDelete={handleClickDelete} />

          <label htmlFor="">Add</label>
          <input onChange={onChangeFile} type="file" />
          <button onClick={uploadFile}>Upload</button>
        </div>
      </div>
      {center.lat && center.lng && (
        <Map
          styles={{ height: "500px", width: "80%", margin: "100px auto" }}
          center={[center.lat, center.lng]}
          zoom={16}
        >
          {path.length > 0 && (
            <>
              <Marker position={[path[0].lat, path[0].lon]} icon={greenIcon} />
              <Marker
                position={[
                  path[path.length - 1].lat,
                  path[path.length - 1].lon,
                ]}
                icon={greenIcon}
              />
              <Polyline
                positions={path}
                pathOptions={{
                  color: "blue",
                  weight: 6,
                }}
              />
            </>
          )}
        </Map>
      )}
    </div>
  );
}

export default App;
